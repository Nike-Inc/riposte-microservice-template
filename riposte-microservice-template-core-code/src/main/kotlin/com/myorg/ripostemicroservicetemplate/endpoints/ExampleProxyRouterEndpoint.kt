package com.myorg.ripostemicroservicetemplate.endpoints

import com.nike.riposte.server.http.ProxyRouterEndpoint
import com.nike.riposte.server.http.RequestInfo
import com.nike.riposte.server.http.impl.SimpleProxyRouterEndpoint
import com.nike.riposte.util.Matcher
import io.netty.channel.ChannelHandlerContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

/**
 * An example of the proxy/router functionality. Since we want the examples to be fully self-contained we simply make a
 * downstream call to our own [ExampleEndpoint.Get] endpoint. This also shows how the incoming request can be tweaked
 * before being passed downstream (see the overridden [getDownstreamRequestFirstChunkInfo] method).
 *
 * NOTE: If your proxy/routing needs are more complex than static incoming->downstream mappings like the example
 * below, you can create a proxy/router endpoint with enormous flexibility while still being straightforward to
 * implement by simply extending [ProxyRouterEndpoint] directly and implementing the
 * [ProxyRouterEndpoint.requestMatcher] and [ProxyRouterEndpoint.getDownstreamRequestFirstChunkInfo] methods.
 *
 * TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
class ExampleProxyRouterEndpoint
@Inject
constructor(@Named("endpoints.port") httpPort: Int,
            @Named("endpoints.sslPort") httpsPort: Int,
            @Named("endpoints.useSsl") useSecure: Boolean
) : SimpleProxyRouterEndpoint(
        Matcher.match("/exampleProxy"),
        "localhost",
        if (useSecure) httpsPort else httpPort,
        ExampleEndpoint.MATCHING_PATH,
        useSecure
) {

    override fun getDownstreamRequestFirstChunkInfo(
            request: RequestInfo<*>, longRunningTaskExecutor: Executor?, ctx: ChannelHandlerContext
    ): CompletableFuture<ProxyRouterEndpoint.DownstreamRequestFirstChunkInfo> {

        // Reuse the super.getDownstreamRequestFirstChunkInfo() impl since it does most of what we want.
        return super.getDownstreamRequestFirstChunkInfo(request, longRunningTaskExecutor, ctx)
                // Relax HTTPS handshake validation requirements since it'll probably be a self-signed cert.
                .thenApply { dsFirstChunk -> dsFirstChunk.withRelaxedHttpsValidation(true) }
                // This is one way you could adjust the downstream request first chunk (headers, etc).
                .whenComplete { dsFirstChunk, _ -> dsFirstChunk.firstChunk.headers().set("foobar", "baz") }
    }
}
