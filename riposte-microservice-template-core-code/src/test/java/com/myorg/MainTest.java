package com.myorg;

import com.nike.riposte.server.config.ServerConfig;
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil;

import com.myorg.ripostemicroservicetemplate.server.config.AppServerConfig;
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.junit.AfterClass;
import org.junit.Test;

import io.restassured.response.ExtractableResponse;

import static com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the functionality of {@link Main}.
 *
 * @author Nic Munroe
 */
public class MainTest {

    @AfterClass
    public static void afterClass() {
        System.clearProperty("@appId");
        System.clearProperty("@environment");
        System.clearProperty("endpoints.port");
        ConfigFactory.invalidateCaches();
    }

    @Test
    public void getServerConfig_creates_AppServerConfig() {
        // given
        System.setProperty("@appId", APP_ID);
        System.setProperty("@environment", "compiletimetest");
        Config configForTesting = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest");
        Main main = new Main();

        // when
        ServerConfig serverConfig = main.getServerConfig(configForTesting);

        // then
        assertThat(serverConfig)
            .isNotNull()
            .isInstanceOf(AppServerConfig.class);
    }

    @Test
    public void public_static_void_main_method_starts_the_server() throws Exception {
        // given
        System.setProperty("@appId", APP_ID);
        System.setProperty("@environment", "compiletimetest");
        int serverPort = TestUtils.findFreePort();
        System.setProperty("endpoints.port", String.valueOf(serverPort));
        // We have to invalidate the typesafe config caches so that it will pick up our endpoints.port system property
        //      override.
        ConfigFactory.invalidateCaches();

        // when
        Main.main(new String[]{});

        // then
        ExtractableResponse healthCheckCallResponse =
            given()
                .baseUri("http://localhost")
                .port(serverPort)
                .log().all()
            .when()
                .basePath("/healthcheck")
                .get()
            .then()
                .log().all()
                .extract();
        assertThat(healthCheckCallResponse.statusCode()).isEqualTo(200);
    }

}