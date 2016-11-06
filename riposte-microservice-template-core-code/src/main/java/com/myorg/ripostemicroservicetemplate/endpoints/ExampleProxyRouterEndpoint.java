package com.myorg.ripostemicroservicetemplate.endpoints;

import com.nike.riposte.server.http.ProxyRouterEndpoint;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.impl.SimpleProxyRouterEndpoint;
import com.nike.riposte.util.Matcher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import io.netty.channel.ChannelHandlerContext;

/**
 * An example of the proxy/router functionality. Since we want the examples to be fully self-contained we simply make a
 * downstream call to our own {@link ExampleEndpoint.Get} endpoint. This
 * also shows how the incoming request can be tweaked before being passed downstream (see the overridden {@link
 * #getDownstreamRequestFirstChunkInfo(RequestInfo, Executor, ChannelHandlerContext)} method).
 *
 * <p>NOTE: If your proxy/routing needs are more complex than static incoming->downstream mappings like the example
 * below, you can create a proxy/router endpoint with enormous flexibility while still being straightforward to
 * implement by simply extending {@link ProxyRouterEndpoint} directly and implementing the {@link
 * ProxyRouterEndpoint#requestMatcher()} and {@link ProxyRouterEndpoint#getDownstreamRequestFirstChunkInfo(RequestInfo,
 * Executor, ChannelHandlerContext)} methods.
 *
 * <p>TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
public class ExampleProxyRouterEndpoint extends SimpleProxyRouterEndpoint {

    @Inject
    public ExampleProxyRouterEndpoint(@Named("endpoints.port") int httpPort,
                                      @Named("endpoints.sslPort") int httpsPort,
                                      @Named("endpoints.useSsl") boolean useSecure) {
        super(Matcher.match("/exampleProxy"),
              "localhost",
              (useSecure) ? httpsPort : httpPort,
              ExampleEndpoint.MATCHING_PATH,
              useSecure);
    }

    @Override
    public CompletableFuture<DownstreamRequestFirstChunkInfo> getDownstreamRequestFirstChunkInfo(
        RequestInfo<?> request, Executor longRunningTaskExecutor, ChannelHandlerContext ctx
    ) {
        // Reuse the super.getDownstreamRequestFirstChunkInfo() impl since it does most of what we want.
        return super.getDownstreamRequestFirstChunkInfo(request, longRunningTaskExecutor, ctx)
                    // Relax HTTPS handshake validation requirements since it'll probably be a self-signed cert.
                    .thenApply(dsFirstChunk -> dsFirstChunk.withRelaxedHttpsValidation(true))
                    // This is one way you could adjust the downstream request first chunk (headers, etc).
                    .whenComplete((dsFirstChunk, error) -> dsFirstChunk.firstChunk.headers().set("foobar", "baz"));
    }
}
