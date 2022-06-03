package com.myorg.ripostemicroservicetemplate.endpoints

import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Verifies the functionality of [HealthCheckEndpoint]
 */
class HealthCheckEndpointTest {

    private val healthCheckEndpoint = HealthCheckEndpoint()

    @Test
    fun healthCheckEndpoint_should_match_all_http_methods() {
        // expect
        assertThat(healthCheckEndpoint.requestMatcher().isMatchAllMethods).isTrue
    }

    @Test
    fun healthCheckEndpoint_should_always_return_http_status_code_200() {
        // when
        val responseFuture = healthCheckEndpoint.execute(mock(), mock(), mock())
        val responseInfo = responseFuture.join()

        // then
        assertThat(responseInfo.httpStatusCode).isEqualTo(200)
    }
}
