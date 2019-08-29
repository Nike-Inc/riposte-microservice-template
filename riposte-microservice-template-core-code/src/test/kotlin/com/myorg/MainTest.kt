package com.myorg

import com.myorg.ripostemicroservicetemplate.server.config.AppServerConfig
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil
import com.typesafe.config.ConfigFactory
import io.restassured.RestAssured.given
import org.assertj.core.api.Assertions.assertThat
import org.junit.AfterClass
import org.junit.Test

/**
 * Tests the functionality of [Main].
 *
 * @author Nic Munroe
 */
class MainTest {

    companion object {

        @AfterClass
        @JvmStatic
        fun afterClass() {
            System.clearProperty("@appId")
            System.clearProperty("@environment")
            System.clearProperty("endpoints.port")
            ConfigFactory.invalidateCaches()
        }
    }

    @Test
    fun getServerConfig_creates_AppServerConfig() {
        // given
        System.setProperty("@appId", APP_ID)
        System.setProperty("@environment", "compiletimetest")
        val configForTesting = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest")
        val main = Main()

        // when
        val serverConfig = main.getServerConfig(configForTesting)

        // then
        assertThat(serverConfig)
                .isNotNull()
                .isInstanceOf(AppServerConfig::class.java)
    }

    @Test
    fun main_method_starts_the_server() {
        // given
        System.setProperty("@appId", APP_ID)
        System.setProperty("@environment", "compiletimetest")
        val serverPort = TestUtils.findFreePort()
        System.setProperty("endpoints.port", serverPort.toString())
        // We have to invalidate the typesafe config caches so that it will pick up our endpoints.port system property
        //      override.
        ConfigFactory.invalidateCaches()

        // when
        main(arrayOf())

        // then
        val healthCheckCallResponse = given()
                .baseUri("http://localhost")
                .port(serverPort)
                .log().all()
            .`when`()
                .basePath("/healthcheck")
                .get()
            .then()
                .log().all()
                .extract()

        assertThat(healthCheckCallResponse.statusCode()).isEqualTo(200)
    }
}
