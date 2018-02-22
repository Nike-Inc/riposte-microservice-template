package com.myorg.ripostemicroservicetemplate.endpoints;

import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.util.Matcher;

import com.myorg.ripostemicroservicetemplate.server.config.guice.AppGuiceModule;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

/**
 * A set of example endpoints that show the Riposte security validation system in action (using basic auth since it's
 * easy to understand).
 * <pre>
 * <ul>
 *     <li>
 *         GET /exampleBasicAuth: This endpoint is *not* basic auth protected, and when you call it you'll get a JSON
 *         response with the auth header you need to add in order to successfully to call the protected
 *         POST /exampleBasicAuth endpoint.
 *     </li>
 *     <li>
 *         POST /exampleBasicAuth: This endpoint *is* basic auth protected. If you call it without the proper auth
 *         header you will get a 401 error. Call GET /exampleBasicAuth (which is not protected) to receive the auth
 *         header value that will allow you to call this endpoint without error.
 *     </li>
 * </ul>
 * </pre>
 *
 * <p>In a real production application you may want to protect all endpoints except /healthcheck. For the examples only
 * POST /exampleBasicAuth is protected. See the comments and implementation of {@link
 * AppGuiceModule#basicAuthProtectedEndpoints(Set)} to see how to switch to protect all endpoints except /healthcheck.
 *
 * <p>TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
public class ExampleBasicAuthProtectedEndpoint {

    public static final String MATCHING_PATH = "/exampleBasicAuth";

    /**
     * The GET implementation of /exampleBasicAuth
     */
    public static class Get extends StandardEndpoint<Void, Map<String, String>> {

        private static final Matcher MATCHER = Matcher.match(MATCHING_PATH, HttpMethod.GET);
        
        private final String basicAuthHeaderValueRequired;

        @Inject
        public Get(
            @Named("exampleBasicAuth.username") String basicAuthUsername,
            @Named("exampleBasicAuth.password") String basicAuthPassword
        ) {
            this.basicAuthHeaderValueRequired = "Basic " + Base64.getEncoder().encodeToString(
                (basicAuthUsername + ":" + basicAuthPassword).getBytes(CharsetUtil.UTF_8)
            );
        }

        @Override
        public CompletableFuture<ResponseInfo<Map<String, String>>> execute(RequestInfo<Void> request,
                                                                            Executor longRunningTaskExecutor,
                                                                            ChannelHandlerContext ctx) {
            Map<String, String> responseData = new LinkedHashMap<>();
            responseData.put("description", "The following Authorization header can be used to call POST "
                                            + MATCHING_PATH + " without a validation error.");
            responseData.put(HttpHeaders.Names.AUTHORIZATION, basicAuthHeaderValueRequired);

            return CompletableFuture.completedFuture(ResponseInfo.newBuilder(responseData).build());
        }

        @Override
        public Matcher requestMatcher() {
            return MATCHER;
        }

    }

    /**
     * The POST implementation of /exampleBasicAuth
     */
    public static class Post extends StandardEndpoint<Void, String> {

        private static final Matcher MATCHER = Matcher.match(MATCHING_PATH, HttpMethod.POST);

        @Override
        public CompletableFuture<ResponseInfo<String>> execute(RequestInfo<Void> request,
                                                               Executor longRunningTaskExecutor,
                                                               ChannelHandlerContext ctx) {

            return CompletableFuture.completedFuture(
                ResponseInfo.newBuilder("Successful Basic Auth call")
                            .withHttpStatusCode(HttpResponseStatus.CREATED.code())
                            .withDesiredContentWriterMimeType("text/plain")
                            .build()
            );
        }

        @Override
        public Matcher requestMatcher() {
            return MATCHER;
        }

    }

}
