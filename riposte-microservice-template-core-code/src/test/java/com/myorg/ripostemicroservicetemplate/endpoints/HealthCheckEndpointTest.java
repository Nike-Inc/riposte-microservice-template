package com.myorg.ripostemicroservicetemplate.endpoints;

import com.nike.riposte.server.http.ResponseInfo;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

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
        // when
        CompletableFuture<ResponseInfo<Void>> responseFuture = healthCheckEndpoint.execute(null, null, null);
        ResponseInfo<Void> responseInfo = responseFuture.join();

        // then
        assertThat(responseInfo.getHttpStatusCode()).isEqualTo(200);
    }

}