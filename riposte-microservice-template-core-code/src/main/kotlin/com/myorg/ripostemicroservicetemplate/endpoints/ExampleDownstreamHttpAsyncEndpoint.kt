package com.myorg.ripostemicroservicetemplate.endpoints

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nike.riposte.client.asynchttp.ning.AsyncHttpClientHelper
import com.nike.riposte.server.http.RequestInfo
import com.nike.riposte.server.http.ResponseInfo
import com.nike.riposte.server.http.StandardEndpoint
import com.nike.riposte.util.Matcher
import io.netty.channel.ChannelHandlerContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named
import org.slf4j.LoggerFactory

/**
 * Contains an example of how to do asynchronous downstream HTTP calls so that the thread count on the server stays
 * constant even when it's being hammered by hundreds or thousands of concurrent requests, each one needing to make
 * downstream calls. Since we want the examples to be fully self-contained we simply make a downstream call to our own
 * [ExampleEndpoint.Get] endpoint. To show how you can do additional work on the response from the downstream call we
 * insert a `"viaAsyncHttpClient": "true"` JSON field into the downstream response before returning.
 *
 * TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
class ExampleDownstreamHttpAsyncEndpoint
@Inject
constructor(
    private val asyncHttpClientHelper: AsyncHttpClientHelper,
    @Named("endpoints.port") httpPort: Int,
    @Named("endpoints.sslPort") httpsPort: Int,
    @Named("endpoints.useSsl") useSecure: Boolean
) : StandardEndpoint<Void, Map<String, Any>>() {

    companion object {
        const val MATCHING_ENDPOINT_PATH = "/exampleDownstreamHttpAsync"
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val objectMapper = ObjectMapper().registerKotlinModule()
    private val downstreamUrl: String

    private val matcher: Matcher = Matcher.match(MATCHING_ENDPOINT_PATH)

    init {
        val localServerHostAndPort =
                if (useSecure)
                    "https://localhost:$httpsPort"
                else
                    "http://localhost:$httpPort"
        this.downstreamUrl = localServerHostAndPort + ExampleEndpoint.MATCHING_PATH
    }

    override fun execute(
        request: RequestInfo<Void>,
        longRunningTaskExecutor: Executor,
        ctx: ChannelHandlerContext
    ): CompletableFuture<ResponseInfo<Map<String, Any>>> {

        val reqWrapper = asyncHttpClientHelper.getRequestBuilder(downstreamUrl, request.method)
        reqWrapper.requestBuilder
                .addQueryParam("some_query_param", "foo")
                .addHeader("Accept", "application/json")

        if (request.rawContentLengthInBytes > 0)
            reqWrapper.requestBuilder.setBody(request.rawContent)

        logger.info("About to make async library call")
        val startTime = System.currentTimeMillis()

        return asyncHttpClientHelper.executeAsyncHttpRequest(reqWrapper, { asyncResponse ->
            logger.info(
                    "In async response handler. Total time spent millis: {}",
                    (System.currentTimeMillis() - startTime)
            )
            ResponseInfo.newBuilder(modifyResponseBody(asyncResponse.responseBody)).build()
        }, ctx)
    }

    private fun modifyResponseBody(rawDownstreamResponse: String): Map<String, Any> {
        val deserializedResponse = objectMapper.readValue<Map<String, Any>>(rawDownstreamResponse).toMutableMap()
        deserializedResponse["viaAsyncHttpClient"] = "true"
        return deserializedResponse
    }

    override fun requestMatcher(): Matcher {
        return matcher
    }
}
