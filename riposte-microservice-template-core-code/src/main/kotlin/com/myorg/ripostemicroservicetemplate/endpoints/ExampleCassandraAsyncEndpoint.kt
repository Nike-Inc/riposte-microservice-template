package com.myorg.ripostemicroservicetemplate.endpoints

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Session
import com.datastax.driver.core.SimpleStatement
import com.myorg.ripostemicroservicetemplate.error.ProjectApiError
import com.nike.backstopper.apierror.ApiError
import com.nike.backstopper.apierror.sample.SampleCoreApiError
import com.nike.backstopper.exception.ApiException
import com.nike.riposte.server.http.RequestInfo
import com.nike.riposte.server.http.ResponseInfo
import com.nike.riposte.server.http.StandardEndpoint
import com.nike.riposte.util.AsyncNettyHelper.functionWithTracingAndMdc
import com.nike.riposte.util.Matcher
import io.netty.channel.ChannelHandlerContext
import net.javacrumbs.futureconverter.java8guava.FutureConverter
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named


/**
 * Endpoint that shows how to do Cassandra calls in an async way using the async driver utilities, without creating
 * extra threads to monitor futures/etc. This maximizes the async nonblocking functionality.
 *
 * NOTE: Don't let the volume of code in here throw you - a large portion of this class is for embedded cassandra
 * which wouldn't be necessary for a non-example project.
 *
 * TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
class ExampleCassandraAsyncEndpoint
@Inject
constructor(
        @Named("disableCassandra") private val disableCassandra: Boolean
) : StandardEndpoint<Void, String>() {

    companion object {

        const val MATCHING_ENDPOINT_PATH = "/exampleCassandraAsync"

        private val basicCassandraQuery = SimpleStatement("SELECT release_version FROM system.local")
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val matcher: Matcher = Matcher.match(MATCHING_ENDPOINT_PATH)

    init {

        // Start up Cassandra as early as possible so it's ready when the first request comes in.
        try {
            // We have to specify the storagedir due to a cassandra-unit bug.
            //      See https://github.com/jsevellec/cassandra-unit/issues/186
            System.setProperty("cassandra.storagedir", "build/embeddedCassandra/storageDir")
            EmbeddedCassandraUtils.startEmbeddedCassandra(disableCassandra)
        } catch (ex: Exception) {
            // No need to prevent the entire app from starting up if there are cassandra problems
            logger.error("Error during embedded cassandra startup", ex)
        }

    }

    override fun execute(request: RequestInfo<Void>, longRunningTaskExecutor: Executor,
                         ctx: ChannelHandlerContext): CompletableFuture<ResponseInfo<String>> {

        val apiErrorToThrowIfSessionMissing: ApiError =
            if (disableCassandra)
                ProjectApiError.EXAMPLE_EMBEDDED_CASSANDRA_DISABLED
            else
                SampleCoreApiError.GENERIC_SERVICE_ERROR

        val session = EmbeddedCassandraUtils.cassandraSession(disableCassandra)
                ?: throw ApiException.newBuilder()
                        .withApiErrors(apiErrorToThrowIfSessionMissing)
                        .withExceptionMessage("Unable to get cassandra session.")
                        .build()

        val cassandraResultFuture = session.executeAsync(basicCassandraQuery)

        // Convert the cassandra result future to a CompletableFuture, then add a listener that turns the result of the
        //      Cassandra call into the ResponseInfo<String> we need to return. Note that we're not doing
        //      thenApplyAsync() because the work done to translate the Cassandra result to our ResponseInfo object is
        //      trivial and doesn't need it's own thread. If you had more complex logic that was time consuming (or more
        //      blocking calls) you would want to do the extra work with CompletableFuture.*Async() calls.
        return FutureConverter
                .toCompletableFuture(cassandraResultFuture)
                .thenApply(functionWithTracingAndMdc( { this.buildResponseFromCassandraQueryResult(it) }, ctx))
    }

    private fun buildResponseFromCassandraQueryResult(result: ResultSet): ResponseInfo<String> {
        logger.info("Building response for async cassandra request")
        return ResponseInfo
                .newBuilder("Cassandra query succeeded. Cassandra version: " + result.one().getString("release_version"))
                .withDesiredContentWriterMimeType("text/text")
                .build()
    }

    override fun requestMatcher(): Matcher {
        return matcher
    }

    /**
     * Contains some static utilities for starting an embedded Cassandra instance. Normally your Guice module would
     * configure whatever cassandra cluster/session you wanted (embedded or otherwise), and you'd `@Inject` the
     * custer/session into your endpoints as needed. But since this is just test/example code tied to
     * ExampleCassandraAsyncEndpoint, we want this code to get wiped away when ExampleCassandraAsyncEndpoint is
     * deleted.
     */
    private object EmbeddedCassandraUtils {

        private val logger = LoggerFactory.getLogger(EmbeddedCassandraUtils::class.java)

        private const val embeddedClusterContactPointHost = "localhost"
        private const val embeddedClusterContactPointPort = 9042
        private const val embeddedClusterWorkDirectory = "build/embeddedCassandra"
        private const val cassandraYamlFile = "/embedded-cassandra.yaml"

        private var cassandraSession: Session? = null

        fun startEmbeddedCassandra(disableCassandra: Boolean): Session? {
            if (disableCassandra) {
                logger.warn(
                        "Embedded cassandra is NOT starting up because your app configuration explicitly requests " +
                        "that it be disabled."
                )
                return null
            }

            if (cassandraSession == null) {
                val cassandraWorkDir = File(embeddedClusterWorkDirectory)
                val cassandraWorkDirAbsolutePath: String = cassandraWorkDir.absolutePath
                if (!cassandraWorkDir.exists()) {
                    logger.info("Creating the  embedded Cassandra folders...{}", cassandraWorkDirAbsolutePath)

                    if (!cassandraWorkDir.mkdirs()) {
                        throw RuntimeException("Unable to create working directory $cassandraWorkDirAbsolutePath")
                    }
                }
                // Start embedded cassandra
                logger.info("Finished Creating the  embedded Cassandra folders...{}", cassandraWorkDirAbsolutePath)
                logger.info("Starting embedded Cassandra")

                try {
                    EmbeddedCassandraServerHelper.startEmbeddedCassandra(cassandraYamlFile, embeddedClusterWorkDirectory)
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }

                val cassandraCluster = Cluster.builder()
                        .addContactPoint(embeddedClusterContactPointHost)
                        .withPort(embeddedClusterContactPointPort)
                        .build()
                cassandraSession = cassandraCluster.connect()
            }

            return cassandraSession
        }

        fun cassandraSession(disableCassandra: Boolean): Session? {
            if (cassandraSession == null) {
                startEmbeddedCassandra(disableCassandra)
            }

            return cassandraSession
        }
    }

}
