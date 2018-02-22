package com.myorg.ripostemicroservicetemplate.componenttest;

import com.nike.backstopper.apierror.sample.SampleCoreApiError;
import com.nike.internal.util.Pair;
import com.nike.riposte.server.Server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint.ErrorHandlingEndpointArgs;
import com.myorg.ripostemicroservicetemplate.error.ProjectApiError;
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import io.restassured.response.ExtractableResponse;

import static com.myorg.ripostemicroservicetemplate.testutils.TestUtils.verifyExpectedError;
import static com.myorg.ripostemicroservicetemplate.testutils.TestUtils.verifyExpectedErrors;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Component test that launches a test version of the application that uses a "compiletimetest" properties file for its
 * environment but is otherwise identical to the real running app. Tests are run against this server to verify that the
 * endpoints are functioning as expected.
 *
 * <p>TODO: EXAMPLE CLEANUP - Delete this class since it is specific to ExampleEndpoint, however it's recommended that
 *          you add similar classes for your real application endpoints.
 *
 * @author Nic Munroe
 */
public class VerifyExampleEndpointComponentTest {

    private static Server realRunningServer;
    private static TestUtils.AppServerConfigForTesting serverConfig;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeClass
    public static void setup() throws Exception {
        Pair<Server, TestUtils.AppServerConfigForTesting> serverAndConfigPair = TestUtils.createServerForTesting();
        serverConfig = serverAndConfigPair.getRight();
        realRunningServer = serverAndConfigPair.getLeft();
        realRunningServer.startup();
    }

    @AfterClass
    public static void teardown() throws Exception {
        realRunningServer.shutdown();
    }

    @Test
    public void example_endpoint_get_call_should_work() throws IOException {
        String responseBodyString =
                given()
                    .baseUri("http://localhost")
                    .port(serverConfig.endpointsPort())
                    .log().all()
                .when()
                    .basePath(ExampleEndpoint.MATCHING_PATH)
                    .get()
                .then()
                    .log().all()
                    .statusCode(200)
                .extract().asString();

        ErrorHandlingEndpointArgs responseBody = objectMapper.readValue(responseBodyString,
                                                                        ErrorHandlingEndpointArgs.class);
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.input_val_1).isNotEmpty();
        assertThat(responseBody.input_val_2).isNotEmpty();
    }

    @Test
    public void example_endpoint_post_call_should_work() throws IOException {
        ErrorHandlingEndpointArgs postBody = new ErrorHandlingEndpointArgs(UUID.randomUUID().toString(),
                                                                           UUID.randomUUID().toString(),
                                                                           false);

        String responseBodyString =
            given()
                .baseUri("http://localhost")
                .port(serverConfig.endpointsPort())
                .body(objectMapper.writeValueAsString(postBody))
                .log().all()
            .when()
                .basePath(ExampleEndpoint.MATCHING_PATH)
                .post()
            .then()
                .log().all()
                .statusCode(201)
                .extract().asString();

        ErrorHandlingEndpointArgs responseBody = objectMapper.readValue(responseBodyString,
                                                                        ErrorHandlingEndpointArgs.class);
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.input_val_1).isEqualTo(postBody.input_val_1);
        assertThat(responseBody.input_val_2).isEqualTo(postBody.input_val_2);
    }

    @Test
    public void example_endpoint_post_call_should_return_validation_errors_when_input_is_invalid() throws IOException {
        ErrorHandlingEndpointArgs postBody = new ErrorHandlingEndpointArgs(null, " \t\n   ", false);

        ExtractableResponse response =
            given()
                .baseUri("http://localhost")
                .port(serverConfig.endpointsPort())
                .body(objectMapper.writeValueAsString(postBody))
                .log().all()
            .when()
                .basePath(ExampleEndpoint.MATCHING_PATH)
                .post()
            .then()
                .log().all()
                .extract();

        verifyExpectedErrors(response, 400, Arrays.asList(ProjectApiError.EXAMPLE_ERROR_BAD_INPUT_VAL_1,
                                                          ProjectApiError.EXAMPLE_ERROR_BAD_INPUT_VAL_2));
    }

    @Test
    public void example_endpoint_post_call_should_return_EXAMPLE_ERROR_MANUALLY_THROWN_when_requested() throws IOException {
        ErrorHandlingEndpointArgs postBody = new ErrorHandlingEndpointArgs("foo", "bar", true);

        ExtractableResponse response =
            given()
                .baseUri("http://localhost")
                .port(serverConfig.endpointsPort())
                .body(objectMapper.writeValueAsString(postBody))
                .log().all()
            .when()
                .basePath(ExampleEndpoint.MATCHING_PATH)
                .post()
            .then()
                .log().all()
                .extract();

        verifyExpectedError(response, ProjectApiError.EXAMPLE_ERROR_MANUALLY_THROWN);
    }

    @Test
    public void example_endpoint_post_call_should_return_MISSING_EXPECTED_CONTENT_when_payload_is_missing() {
        ExtractableResponse response =
            given()
                .baseUri("http://localhost")
                .port(serverConfig.endpointsPort())
                .log().all()
            .when()
                .basePath(ExampleEndpoint.MATCHING_PATH)
                .post()
            .then()
                .log().all()
                .extract();

        verifyExpectedError(response, SampleCoreApiError.MISSING_EXPECTED_CONTENT);
    }
}
