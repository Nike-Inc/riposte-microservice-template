package com.myorg.ripostemicroservicetemplate.functionaltest;

import com.nike.backstopper.apierror.sample.SampleCoreApiError;

import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint;

import org.junit.Test;

import io.restassured.response.ExtractableResponse;

import static io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional test that verifies the server is correctly restricting access via basic auth.
 *
 * <p>TODO: EXAMPLE CLEANUP - If your app does not use any security validation (e.g. basic auth for the examples) then
 *          you can delete this class entirely. If you do use security validation then you'll want to adjust these tests
 *          to tests your real endpoint security rather than the example stuff.
 *
 * @author Nic Munroe
 */
public class BasicAuthVerificationFunctionalTest {

    private final PropertiesHelper props = PropertiesHelper.getInstance();

    @Test
    public void verify_call_works_with_valid_basic_auth_header() {
        String fullUrl = props.ripostemicroservicetemplateHost + ExampleBasicAuthProtectedEndpoint.MATCHING_PATH;

        ExtractableResponse response =
            given()
                .header(AUTHORIZATION, props.basicAuthHeaderVal)
                .log().all()
            .when()
                .post(fullUrl)
            .then()
                .log().all()
                .extract();

        int responseCode = response.statusCode();
        assertThat(responseCode).isEqualTo(201);
    }

    @Test
    public void verify_call_fails_with_invalid_basic_auth_header() {
        String fullUrl = props.ripostemicroservicetemplateHost + ExampleBasicAuthProtectedEndpoint.MATCHING_PATH;

        ExtractableResponse response =
            given()
                .header(AUTHORIZATION, "foo" + props.basicAuthHeaderVal)
                .log().all()
            .when()
                .post(fullUrl)
            .then()
                .log().all()
                .extract();

        props.verifyExpectedError(response, SampleCoreApiError.UNAUTHORIZED);
    }

    @Test
    public void healthcheck_call_should_not_require_basic_auth() {
        String fullUrl = props.ripostemicroservicetemplateHost + "/healthcheck";

        given()
            .log().all()
        .when()
            .get(fullUrl)
        .then()
            .log().all()
            .statusCode(200);
    }

}
