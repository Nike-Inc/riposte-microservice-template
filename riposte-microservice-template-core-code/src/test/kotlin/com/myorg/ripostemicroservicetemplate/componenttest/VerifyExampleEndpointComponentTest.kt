package com.myorg.ripostemicroservicetemplate.componenttest

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint.ErrorHandlingEndpointArgs
import com.myorg.ripostemicroservicetemplate.error.ProjectApiError
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.verifyExpectedError
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.verifyExpectedErrors
import com.nike.backstopper.apierror.sample.SampleCoreApiError
import com.nike.riposte.server.Server
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.UUID

/**
 * Component test that launches a test version of the application that uses a "compiletimetest" properties file for its
 * environment but is otherwise identical to the real running app. Tests are run against this server to verify that the
 * endpoints are functioning as expected.
 *
 *
 * TODO: EXAMPLE CLEANUP - Delete this class since it is specific to ExampleEndpoint, however it's recommended that
 * you add similar classes for your real application endpoints.
 *
 * @author Nic Munroe
 */
class VerifyExampleEndpointComponentTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun example_endpoint_get_call_should_work() {
        val responseBodyString = given()
            .baseUri("http://localhost")
            .port(serverConfig!!.endpointsPort())
            .log().all()
            .`when`()
            .basePath(ExampleEndpoint.MATCHING_PATH)
            .get()
            .then()
            .log().all()
            .statusCode(200)
            .extract().asString()

        val responseBody = objectMapper.readValue<ErrorHandlingEndpointArgs>(responseBodyString)
        assertThat(responseBody).isNotNull()
        assertThat(responseBody.input_val_1).isNotEmpty()
        assertThat(responseBody.input_val_2).isNotEmpty()
    }

    @Test
    fun example_endpoint_post_call_should_work() {
        val postBody = ErrorHandlingEndpointArgs(
            UUID.randomUUID().toString(), UUID.randomUUID().toString(), false
        )

        val responseBodyString = given()
            .baseUri("http://localhost")
            .port(serverConfig!!.endpointsPort())
            .body(objectMapper.writeValueAsString(postBody))
            .log().all()
            .`when`()
            .basePath(ExampleEndpoint.MATCHING_PATH)
            .post()
            .then()
            .log().all()
            .statusCode(201)
            .extract().asString()

        val responseBody = objectMapper.readValue<ErrorHandlingEndpointArgs>(responseBodyString)
        assertThat(responseBody).isNotNull()
        assertThat(responseBody.input_val_1).isEqualTo(postBody.input_val_1)
        assertThat(responseBody.input_val_2).isEqualTo(postBody.input_val_2)
    }

    @Test
    fun example_endpoint_post_call_should_return_validation_errors_when_input_is_empty() {
        val postBody = ErrorHandlingEndpointArgs(null, " \t\n   ", false)

        val response = given()
            .baseUri("http://localhost")
            .port(serverConfig!!.endpointsPort())
            .body(objectMapper.writeValueAsString(postBody))
            .log().all()
            .`when`()
            .basePath(ExampleEndpoint.MATCHING_PATH)
            .post()
            .then()
            .log().all()
            .extract()

        verifyExpectedErrors(
            response,
            400,
            listOf(ProjectApiError.EXAMPLE_ERROR_BAD_INPUT_VAL_1, ProjectApiError.EXAMPLE_ERROR_BAD_INPUT_VAL_2)
        )
    }

    @Test
    fun example_endpoint_post_call_should_return_validation_errors_when_input_is_too_long() {
        val postBody = ErrorHandlingEndpointArgs(
            "123456789012345678901234567890123456789012345678901", // 51 chars
            "1234567890123456789012345678901234567890123456789012345678901", // 61 chars
            false
        )

        val response = given()
            .baseUri("http://localhost")
            .port(serverConfig!!.endpointsPort())
            .body(objectMapper.writeValueAsString(postBody))
            .log().all()
            .`when`()
            .basePath(ExampleEndpoint.MATCHING_PATH)
            .post()
            .then()
            .log().all()
            .extract()

        verifyExpectedErrors(
            response,
            400,
            listOf(
                ProjectApiError.EXAMPLE_ERROR_BAD_INPUT_VAL_1_TOO_LARGE,
                ProjectApiError.EXAMPLE_ERROR_BAD_INPUT_VAL_2_TOO_LARGE
            )
        )
    }

    @Test
    fun example_endpoint_post_call_should_return_EXAMPLE_ERROR_MANUALLY_THROWN_when_requested() {
        val postBody = ErrorHandlingEndpointArgs("foo", "bar", true)

        val response = given()
            .baseUri("http://localhost")
            .port(serverConfig!!.endpointsPort())
            .body(objectMapper.writeValueAsString(postBody))
            .log().all()
            .`when`()
            .basePath(ExampleEndpoint.MATCHING_PATH)
            .post()
            .then()
            .log().all()
            .extract()

        verifyExpectedError(response, ProjectApiError.EXAMPLE_ERROR_MANUALLY_THROWN)
    }

    @Test
    fun example_endpoint_post_call_should_return_MISSING_EXPECTED_CONTENT_when_payload_is_missing() {
        val response = given()
            .baseUri("http://localhost")
            .port(serverConfig!!.endpointsPort())
            .log().all()
            .`when`()
            .basePath(ExampleEndpoint.MATCHING_PATH)
            .post()
            .then()
            .log().all()
            .extract()

        verifyExpectedError(response, SampleCoreApiError.MISSING_EXPECTED_CONTENT)
    }

    companion object {

        private var realRunningServer: Server? = null
        private var serverConfig: TestUtils.AppServerConfigForTesting? = null

        @BeforeClass
        @JvmStatic
        fun setup() {
            val serverAndConfigPair = TestUtils.createServerForTesting()
            serverConfig = serverAndConfigPair.right
            realRunningServer = serverAndConfigPair.left
            realRunningServer!!.startup()
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            realRunningServer!!.shutdown()
        }
    }
}
