package com.myorg.ripostemicroservicetemplate.componenttest;

import com.nike.backstopper.apierror.sample.SampleCoreApiError;
import com.nike.internal.util.Pair;
import com.nike.riposte.server.Server;

import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint;
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Base64;

import io.netty.util.CharsetUtil;
import io.restassured.response.ExtractableResponse;

import static com.myorg.ripostemicroservicetemplate.testutils.TestUtils.verifyExpectedError;
import static io.netty.handler.codec.http.HttpHeaders.Names.AUTHORIZATION;
import static io.restassured.RestAssured.given;

/**
 * Component test that verifies the basic auth security is configured correctly on the server.
 *
 * <p>TODO: EXAMPLE CLEANUP - If your app does not use any security validation (e.g. basic auth for the examples) then
 *          you can delete this class entirely. If you do use security validation then you'll want to adjust these tests
 *          to tests your real endpoint security rather than the example stuff.
 *
 * @author Nic Munroe
 */
public class VerifyBasicAuthIsConfiguredCorrectlyComponentTest {

    private static Server realRunningServer;
    private static TestUtils.AppServerConfigForTesting serverConfig;

    private static String basicAuthHeaderValueRequired;

    @BeforeClass
    public static void setup() throws Exception {
        Pair<Server, TestUtils.AppServerConfigForTesting> serverAndConfigPair = TestUtils.createServerForTesting();
        serverConfig = serverAndConfigPair.getRight();
        realRunningServer = serverAndConfigPair.getLeft();
        realRunningServer.startup();

        String basicAuthUsername = serverConfig.getTestingAppConfig().getString("exampleBasicAuth.username");
        String basicAuthPassword = serverConfig.getTestingAppConfig().getString("exampleBasicAuth.password");
        basicAuthHeaderValueRequired = "Basic " + Base64.getEncoder().encodeToString(
            (basicAuthUsername + ":" + basicAuthPassword).getBytes(CharsetUtil.UTF_8)
        );
    }

    @AfterClass
    public static void teardown() throws Exception {
        realRunningServer.shutdown();
    }

    @Test
    public void endpoint_call_should_work_with_valid_basic_auth_header() {
        given()
            .baseUri("http://localhost")
            .port(serverConfig.endpointsPort())
            .header(AUTHORIZATION, basicAuthHeaderValueRequired)
            .log().all()
        .when()
            .basePath(ExampleBasicAuthProtectedEndpoint.MATCHING_PATH)
            .post()
        .then()
            .log().all()
            .statusCode(201);
    }

    @Test
    public void endpoint_call_should_fail_with_invalid_basic_auth_header() {
        ExtractableResponse response =
            given()
                .baseUri("http://localhost")
                .port(serverConfig.endpointsPort())
                .header(AUTHORIZATION, "foo" + basicAuthHeaderValueRequired)
                .log().all()
            .when()
                .basePath(ExampleBasicAuthProtectedEndpoint.MATCHING_PATH)
                .post()
            .then()
                .log().all()
                .extract();

        verifyExpectedError(response, SampleCoreApiError.UNAUTHORIZED);
    }

    @Test
    public void healthcheck_call_should_not_require_basic_auth() {
        given()
            .baseUri("http://localhost")
            .port(serverConfig.endpointsPort())
            .log().all()
        .when()
            .basePath("/healthcheck")
            .get()
        .then()
            .log().all()
            .statusCode(200)
            .extract().asString();
    }

}
