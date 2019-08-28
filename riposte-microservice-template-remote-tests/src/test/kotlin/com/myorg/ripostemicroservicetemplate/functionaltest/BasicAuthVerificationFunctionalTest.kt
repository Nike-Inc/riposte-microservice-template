package com.myorg.ripostemicroservicetemplate.functionaltest

import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint
import com.nike.backstopper.apierror.sample.SampleCoreApiError
import io.netty.handler.codec.http.HttpHeaderNames.AUTHORIZATION
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.myorg.ripostemicroservicetemplate.functionaltest.PropertiesHelper.Companion.INSTANCE as props

/**
 * Functional test that verifies the server is correctly restricting access via basic auth.
 *
 * TODO: EXAMPLE CLEANUP - If your app does not use any security validation (e.g. basic auth for the examples) then
 * you can delete this class entirely. If you do use security validation then you'll want to adjust these tests
 * to tests your real endpoint security rather than the example stuff.
 *
 * @author Nic Munroe
 */
class BasicAuthVerificationFunctionalTest {

    @Test
    fun verify_call_works_with_valid_basic_auth_header() {
        val fullUrl = props.ripostemicroservicetemplateHost + ExampleBasicAuthProtectedEndpoint.MATCHING_PATH

        val response = given()
                .header(AUTHORIZATION.toString(), props.basicAuthHeaderVal)
                .log().all()
            .`when`()
                .post(fullUrl)
            .then()
                .log().all()
                .extract()

        val responseCode = response.statusCode()
        assertThat(responseCode).isEqualTo(201)
    }

    @Test
    fun verify_call_fails_with_invalid_basic_auth_header() {
        val fullUrl = props.ripostemicroservicetemplateHost + ExampleBasicAuthProtectedEndpoint.MATCHING_PATH

        val response = given()
                .header(AUTHORIZATION.toString(), "foo" + props.basicAuthHeaderVal)
                .log().all()
            .`when`()
                .post(fullUrl)
            .then()
                .log().all()
                .extract()

        props.verifyExpectedError(response, SampleCoreApiError.UNAUTHORIZED)
    }

    @Test
    fun healthcheck_call_should_not_require_basic_auth() {
        val fullUrl = props.ripostemicroservicetemplateHost + "/healthcheck"

        given()
                .log().all()
            .`when`()
                .get(fullUrl)
            .then()
                .log().all()
                .statusCode(200)
    }

}
