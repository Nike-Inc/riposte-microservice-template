package com.myorg.ripostemicroservicetemplate.componenttest

import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.verifyExpectedError
import com.nike.backstopper.apierror.sample.SampleCoreApiError
import com.nike.riposte.server.Server
import io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION
import io.netty.util.CharsetUtil
import io.restassured.RestAssured.given
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.io.IOException
import java.util.Base64

/**
 * Component test that verifies the basic auth security is configured correctly on the server.
 *
 * TODO: EXAMPLE CLEANUP - If your app does not use any security validation (e.g. basic auth for the examples) then
 * you can delete this class entirely. If you do use security validation then you'll want to adjust these tests
 * to tests your real endpoint security rather than the example stuff.
 *
 * @author Nic Munroe
 */
class VerifyBasicAuthIsConfiguredCorrectlyComponentTest {

    @Test
    fun endpoint_call_should_work_with_valid_basic_auth_header() {
        given()
                .baseUri("http://localhost")
                .port(serverConfig!!.endpointsPort())
                .header(AUTHORIZATION, basicAuthHeaderValueRequired)
                .log().all()
            .`when`()
                .basePath(ExampleBasicAuthProtectedEndpoint.MATCHING_PATH)
                .post()
            .then()
                .log().all()
                .statusCode(201)
    }

    @Test
    @Throws(IOException::class)
    fun endpoint_call_should_fail_with_invalid_basic_auth_header() {
        val response = given()
                .baseUri("http://localhost")
                .port(serverConfig!!.endpointsPort())
                .header(AUTHORIZATION, "foo" + basicAuthHeaderValueRequired!!)
                .log().all()
            .`when`()
                .basePath(ExampleBasicAuthProtectedEndpoint.MATCHING_PATH)
                .post()
            .then()
                .log().all()
                .extract()

        verifyExpectedError(response, SampleCoreApiError.UNAUTHORIZED)
    }

    @Test
    fun healthcheck_call_should_not_require_basic_auth() {
        given()
                .baseUri("http://localhost")
                .port(serverConfig!!.endpointsPort())
                .log().all()
            .`when`()
                .basePath("/healthcheck")
                .get()
            .then()
                .log().all()
                .statusCode(200)
                .extract().asString()
    }

    companion object {

        private var realRunningServer: Server? = null
        private var serverConfig: TestUtils.AppServerConfigForTesting? = null

        private var basicAuthHeaderValueRequired: String? = null

        @BeforeClass
        @JvmStatic
        fun setup() {
            val serverAndConfigPair = TestUtils.createServerForTesting()
            serverConfig = serverAndConfigPair.right
            realRunningServer = serverAndConfigPair.left
            realRunningServer!!.startup()

            val basicAuthUsername = serverConfig!!.testingAppConfig.getString("exampleBasicAuth.username")
            val basicAuthPassword = serverConfig!!.testingAppConfig.getString("exampleBasicAuth.password")
            basicAuthHeaderValueRequired = "Basic " + Base64.getEncoder().encodeToString(
                    (basicAuthUsername + ":" + basicAuthPassword).toByteArray(CharsetUtil.UTF_8)
            )
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            realRunningServer!!.shutdown()
        }
    }

}
