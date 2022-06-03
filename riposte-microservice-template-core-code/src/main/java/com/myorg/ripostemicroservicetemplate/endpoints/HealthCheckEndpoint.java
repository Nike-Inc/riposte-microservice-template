package com.myorg.ripostemicroservicetemplate.endpoints;

import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.util.Matcher;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import io.netty.channel.ChannelHandlerContext;

/**
 * Dummy health check endpoint. This will immediately respond with a 200 HTTP status code. It will let you know when
 * your machine has fallen over but not much else (which may be sufficient depending on your use case).
 */
public class HealthCheckEndpoint extends StandardEndpoint<Void, Void> {

    private static final Matcher MATCHER = Matcher.match("/healthcheck");

    @Override
    public @NotNull CompletableFuture<ResponseInfo<Void>> execute(
        @NotNull RequestInfo<Void> request,
        @NotNull Executor longRunningTaskExecutor,
        @NotNull ChannelHandlerContext ctx
    ) {
        return CompletableFuture.completedFuture(ResponseInfo.<Void>newBuilder().withHttpStatusCode(200).build());
    }

    @Override
    public @NotNull Matcher requestMatcher() {
        return MATCHER;
    }
}
