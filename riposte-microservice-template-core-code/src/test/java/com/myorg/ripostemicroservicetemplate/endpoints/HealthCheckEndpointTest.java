package com.myorg.ripostemicroservicetemplate.endpoints;

import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import io.netty.channel.ChannelHandlerContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Verifies the functionality of {@link HealthCheckEndpoint}
 *
 * @author Nic Munroe
 */
public class HealthCheckEndpointTest {

    private HealthCheckEndpoint healthCheckEndpoint = new HealthCheckEndpoint();

    @Test
    public void healthCheckEndpoint_should_match_all_http_methods() {
        // expect
        assertThat(healthCheckEndpoint.requestMatcher().isMatchAllMethods()).isTrue();
    }

    @Test
    public void healthCheckEndpoint_should_always_return_http_status_code_200() {
        // given
        @SuppressWarnings("unchecked")
        RequestInfo<Void> requestMock = mock(RequestInfo.class);
        Executor executorMock = mock(Executor.class);
        ChannelHandlerContext ctxMock = mock(ChannelHandlerContext.class);

        // when
        CompletableFuture<ResponseInfo<Void>> responseFuture = healthCheckEndpoint.execute(
            requestMock, executorMock, ctxMock
        );
        ResponseInfo<Void> responseInfo = responseFuture.join();

        // then
        assertThat(responseInfo.getHttpStatusCode()).isEqualTo(200);
    }

}