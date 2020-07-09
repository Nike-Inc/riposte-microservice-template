package com.myorg.ripostemicroservicetemplate.endpoints

import com.myorg.ripostemicroservicetemplate.server.config.guice.AppSecurityGuiceModule
import com.nike.riposte.server.http.RequestInfo
import com.nike.riposte.server.http.ResponseInfo
import com.nike.riposte.server.http.StandardEndpoint
import com.nike.riposte.util.Matcher
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.util.CharsetUtil
import java.util.Base64
import java.util.LinkedHashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

/**
 * A set of example endpoints that show the Riposte security validation system in action (using basic auth since it's
 * easy to understand).
 *
 * - `GET /exampleBasicAuth`: This endpoint is *not* basic auth protected, and when you call it you'll get a JSON
 * response with the auth header you need to add in order to successfully to call the protected
 * `POST /exampleBasicAuth` endpoint.
 * - `POST /exampleBasicAuth`: This endpoint *is* basic auth protected. If you call it without the proper auth
 * header you will get a 401 error. Call `GET /exampleBasicAuth` (which is not protected) to receive the auth
 * header value that will allow you to call this endpoint without error.
 *
 * In a real production application you may want to protect all endpoints except `/healthcheck`. For the examples
 * only `POST /exampleBasicAuth` is protected. See the comments and implementation of
 * [AppSecurityGuiceModule.authProtectedEndpoints] to see how to switch to protect all endpoints except `/healthcheck`.
 *
 * TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
object ExampleBasicAuthProtectedEndpoint {

    const val MATCHING_PATH = "/exampleBasicAuth"

    /**
     * The GET implementation of /exampleBasicAuth
     */
    class Get
    @Inject
    constructor(
        @Named("exampleBasicAuth.username") basicAuthUsername: String,
        @Named("exampleBasicAuth.password") basicAuthPassword: String
    ) : StandardEndpoint<Void, Map<String, String>>() {

        private val matcher: Matcher = Matcher.match(MATCHING_PATH, HttpMethod.GET)
        private val basicAuthHeaderValueRequired: String =
                "Basic " + Base64.getEncoder().encodeToString(
                        ("$basicAuthUsername:$basicAuthPassword").toByteArray(CharsetUtil.UTF_8)
                )

        override fun execute(
            request: RequestInfo<Void>,
            longRunningTaskExecutor: Executor,
            ctx: ChannelHandlerContext
        ): CompletableFuture<ResponseInfo<Map<String, String>>> {

            val responseData = LinkedHashMap<String, String>()
            responseData["description"] = "The following Authorization header can be used to call " +
                    "POST $MATCHING_PATH without a validation error."
            responseData[HttpHeaderNames.AUTHORIZATION.toString()] = basicAuthHeaderValueRequired

            return CompletableFuture.completedFuture(
                    ResponseInfo.newBuilder<Map<String, String>>(responseData).build()
            )
        }

        override fun requestMatcher(): Matcher {
            return matcher
        }
    }

    /**
     * The POST implementation of /exampleBasicAuth
     */
    class Post : StandardEndpoint<Void, String>() {

        private val matcher: Matcher = Matcher.match(MATCHING_PATH, HttpMethod.POST)

        override fun execute(
            request: RequestInfo<Void>,
            longRunningTaskExecutor: Executor,
            ctx: ChannelHandlerContext
        ): CompletableFuture<ResponseInfo<String>> {

            return CompletableFuture.completedFuture(
                    ResponseInfo.newBuilder("Successful Basic Auth call")
                            .withHttpStatusCode(HttpResponseStatus.CREATED.code())
                            .withDesiredContentWriterMimeType("text/plain")
                            .build()
            )
        }

        override fun requestMatcher(): Matcher {
            return matcher
        }
    }
}
