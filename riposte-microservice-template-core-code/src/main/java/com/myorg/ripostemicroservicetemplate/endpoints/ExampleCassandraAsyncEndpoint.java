package com.myorg.ripostemicroservicetemplate.endpoints;

import com.nike.backstopper.apierror.sample.SampleCoreApiError;
import com.nike.backstopper.exception.ApiException;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.util.Matcher;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

import net.javacrumbs.futureconverter.java8guava.FutureConverter;

import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import io.netty.channel.ChannelHandlerContext;

import static com.nike.riposte.util.AsyncNettyHelper.functionWithTracingAndMdc;

/**
 * Endpoint that shows how to do Cassandra calls in an async way using the async driver utilities, without creating
 * extra threads to monitor futures/etc. This maximizes the async nonblocking functionality.
 *
 * <p>NOTE: Don't let the volume of code in here throw you - a large portion of this class is for embedded cassandra
 * which wouldn't be necessary for a non-example project.
 *
 * <p>TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
@SuppressWarnings("WeakerAccess")
public class ExampleCassandraAsyncEndpoint extends StandardEndpoint<Void, String> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String MATCHING_ENDPOINT_PATH = "/exampleCassandraAsync";
    private static final Matcher MATCHER = Matcher.match(MATCHING_ENDPOINT_PATH);

    private static final Statement basicCassandraQuery =
        new SimpleStatement("SELECT release_version FROM system.local");

    private final boolean disableCassandra;

    @Inject
    public ExampleCassandraAsyncEndpoint(@Named("disableCassandra") Boolean disableCassandra) {
        this.disableCassandra = disableCassandra;

        // Start up Cassandra as early as possible so it's ready when the first request comes in.
        try {
            // We have to specify the storagedir due to a cassandra-unit bug.
            //      See https://github.com/jsevellec/cassandra-unit/issues/186
            System.setProperty("cassandra.storagedir", "build/embeddedCassandra/storageDir");
            EmbeddedCassandraUtils.startEmbeddedCassandra(disableCassandra);
        }
        catch (Throwable ex) {
            // No need to prevent the entire app from starting up if there are cassandra problems
            logger.error("Error during embedded cassandra startup", ex);
        }
    }

    @Override
    public CompletableFuture<ResponseInfo<String>> execute(RequestInfo<Void> request, Executor longRunningTaskExecutor,
                                                           ChannelHandlerContext ctx) {

        Session session = EmbeddedCassandraUtils.cassandraSession(disableCassandra);
        if (session == null) {
            throw ApiException.newBuilder()
                              .withApiErrors(SampleCoreApiError.GENERIC_SERVICE_ERROR)
                              .withExceptionMessage("Unable to get cassandra session.")
                              .build();
        }

        ResultSetFuture cassandraResultFuture = session.executeAsync(basicCassandraQuery);

        // Convert the cassandra result future to a CompletableFuture, then add a listener that turns the result of the
        //      Cassandra call into the ResponseInfo<String> we need to return. Note that we're not doing
        //      thenApplyAsync() because the work done to translate the Cassandra result to our ResponseInfo object is
        //      trivial and doesn't need it's own thread. If you had more complex logic that was time consuming (or more
        //      blocking calls) you would want to do the extra work with CompletableFuture.*Async() calls.
        return FutureConverter
            .toCompletableFuture(cassandraResultFuture)
            .thenApply(functionWithTracingAndMdc(this::buildResponseFromCassandraQueryResult, ctx));
    }

    private ResponseInfo<String> buildResponseFromCassandraQueryResult(ResultSet result) {
        logger.info("Building response for async cassandra request");
        return ResponseInfo
            .newBuilder("Cassandra query succeeded. Cassandra version: " + result.one().getString("release_version"))
            .withDesiredContentWriterMimeType("text/text")
            .build();
    }

    @Override
    public Matcher requestMatcher() {
        return MATCHER;
    }

    /**
     * Contains some static utilities for starting an embedded Cassandra instance. Normally your Guice module would
     * configure whatever cassandra cluster/session you wanted (embedded or otherwise), and you'd {@code @Inject} the
     * custer/session into your endpoints as needed. But since this is just test/example code tied to
     * ExampleCassandraAsyncEndpoint, we want this code to get wiped away when ExampleCassandraAsyncEndpoint is
     * deleted.
     */
    public static class EmbeddedCassandraUtils {

        private static final Logger logger = LoggerFactory.getLogger(EmbeddedCassandraUtils.class);

        private static final String embeddedClusterContactPointHost = "localhost";
        private static final int embeddedClusterContactPointPort = 9042;
        private static final String embeddedClusterWorkDirectory = "build/embeddedCassandra";
        private static final String cassandraYamlFile = "/embedded-cassandra.yaml";

        private static Session cassandraSession = null;

        private static Session startEmbeddedCassandra(boolean disableCassandra) {
            if (disableCassandra) {
                logger.warn("Embedded cassandra is NOT starting up because your app configuration explicitly requests "
                            + "that it be disabled.");
                return null;
            }

            if (cassandraSession == null) {
                File cassandraWorkDir = new File(embeddedClusterWorkDirectory);
                if (!cassandraWorkDir.exists()) {
                    logger.info("Creating the  embedded Cassandra folders...{}", cassandraWorkDir.getAbsolutePath());
                    //noinspection ResultOfMethodCallIgnored
                    cassandraWorkDir.mkdirs();
                }
                // Start embedded cassandra
                logger.info("Finished Creating the  embedded Cassandra folders...{}",
                            cassandraWorkDir.getAbsolutePath());
                logger.info("Starting embedded Cassandra");

                try {
                    EmbeddedCassandraServerHelper.startEmbeddedCassandra(cassandraYamlFile,
                                                                         embeddedClusterWorkDirectory);
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }

                Cluster cassandraCluster = Cluster.builder()
                                                  .addContactPoint(embeddedClusterContactPointHost)
                                                  .withPort(embeddedClusterContactPointPort)
                                                  .build();
                cassandraSession = cassandraCluster.connect();
            }

            return cassandraSession;
        }

        public static Session cassandraSession(boolean disableCassandra) {
            if (cassandraSession == null) {
                startEmbeddedCassandra(disableCassandra);
            }

            return cassandraSession;
        }
    }
}
