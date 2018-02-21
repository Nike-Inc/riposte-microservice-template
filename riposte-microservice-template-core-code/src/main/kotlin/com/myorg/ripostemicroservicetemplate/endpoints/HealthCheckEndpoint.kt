package com.myorg.ripostemicroservicetemplate.endpoints

import com.nike.riposte.server.http.RequestInfo
import com.nike.riposte.server.http.ResponseInfo
import com.nike.riposte.server.http.StandardEndpoint
import com.nike.riposte.util.Matcher
import io.netty.channel.ChannelHandlerContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * Dummy health check endpoint. This will immediately respond with a 200 HTTP status code. It will let you know when
 * your machine has fallen over but not much else (which may be sufficient depending on your use case).
 */
class HealthCheckEndpoint : StandardEndpoint<Void, Void>() {

    private val matcher = Matcher.match("/healthcheck")

    override fun execute(
        request: RequestInfo<Void>,
        longRunningTaskExecutor: Executor,
        ctx: ChannelHandlerContext
    ): CompletableFuture<ResponseInfo<Void>> {
        return CompletableFuture.completedFuture(
            ResponseInfo.newBuilder<Void>().withHttpStatusCode(200).build()
        )
    }

    override fun requestMatcher(): Matcher {
        return matcher
    }
}
