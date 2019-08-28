package com.myorg.ripostemicroservicetemplate.server.config

import com.authzee.kotlinguice4.typeLiteral
import com.google.inject.Module
import com.google.inject.util.Modules
import com.google.inject.util.Providers
import com.myorg.ripostemicroservicetemplate.server.config.guice.AppGuiceModule
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID
import com.nike.backstopper.handler.riposte.RiposteApiExceptionHandler
import com.nike.backstopper.handler.riposte.RiposteUnhandledExceptionHandler
import com.nike.backstopper.service.riposte.BackstopperRiposteValidatorAdapter
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener
import com.nike.riposte.metrics.codahale.impl.EndpointMetricsHandlerDefaultImpl
import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

/**
 * Tests the functionality of [AppServerConfig]
 *
 * @author Nic Munroe
 */
class AppServerConfigTest {

    private var configForTesting: Config? = null
    private var appServerConfig: AppServerConfig? = null

    @Before
    fun beforeMethod() {
        System.setProperty("@appId", APP_ID)
        System.setProperty("@environment", "compiletimetest")
        configForTesting = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest")
        appServerConfig = AppServerConfig(configForTesting)
    }

    @Test
    fun constructor_fails_if_passed_null_appConfig() {
        // when
        val thrown = catchThrowable { AppServerConfig(null) }

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("appConfig")
    }

    @Test
    fun constructor_calls_initEndpointAndServerConfigMetrics_on_metricsListener() {
        // given
        val asc = AppServerConfig(configForTesting)

        // expect
        val emh = (asc.metricsListener() as CodahaleMetricsListener).endpointMetricsHandler
        assertThat(emh).isInstanceOf(EndpointMetricsHandlerDefaultImpl::class.java)
        assertThat((emh as EndpointMetricsHandlerDefaultImpl).endpointRequestsTimers).isNotEmpty
    }

    @Test
    fun constructor_does_not_blow_up_if_metricsListener_is_null() {
        // given
        val asc = object : AppServerConfig(configForTesting) {
            override fun getAppGuiceModules(appConfig: Config): List<Module> {
                val modules = ArrayList(super.getAppGuiceModules(appConfig))
                val appModule = getModuleOfType(modules, AppGuiceModule::class.java)
                modules.remove(appModule)
                modules.add(
                        Modules.override(AppGuiceModule(configForTesting))
                                .with(
                                        Module {
                                            binder ->
                                            binder.bind(typeLiteral<CodahaleMetricsListener>())
                                                    .toProvider(Providers.of<CodahaleMetricsListener>(null))
                                        }
                                )
                )

                return modules
            }
        }

        // expect
        assertThat(asc.metricsListener()).isNull()
    }

    private fun <T : Module> getModuleOfType(modules: List<Module>, desiredType: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return modules.firstOrNull { desiredType.isInstance(it) } as T
    }

    @Test
    fun a_BackstopperRiposteConfigGuiceModule_is_used_to_setup_error_handlers_and_validators() {
        // expect
        assertThat(appServerConfig!!.riposteErrorHandler())
                .isInstanceOf(RiposteApiExceptionHandler::class.java)
        assertThat(appServerConfig!!.riposteUnhandledErrorHandler())
                .isInstanceOf(RiposteUnhandledExceptionHandler::class.java)
        assertThat(appServerConfig!!.requestContentValidationService())
                .isInstanceOf(BackstopperRiposteValidatorAdapter::class.java)
    }

    @Test
    fun accessLogger_returns_non_null_object() {
        // when
        val obj = appServerConfig!!.accessLogger()

        // then
        assertThat(obj).isNotNull()
    }

    @Test
    fun appInfo_returns_non_null_object() {
        // when
        val obj = appServerConfig!!.appInfo()

        // then
        assertThat(obj).isNotNull
        assertThat(obj.join()).isNotNull()
    }

    @Test
    fun metricsListener_returns_non_null_object() {
        // when
        val obj = appServerConfig!!.metricsListener()

        // then
        assertThat(obj).isNotNull()
    }

    @Test
    fun metricsListener_returns_null_object_if_no_metrics_reporters_are_enabled() {
        // given
        val configNoReporters = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest")
                .withValue("metrics.slf4j.reporting.enabled", ConfigValueFactory.fromAnyRef(false))
                .withValue("metrics.graphite.reporting.enabled", ConfigValueFactory.fromAnyRef(false))
                .withValue("metrics.jmx.reporting.enabled", ConfigValueFactory.fromAnyRef(false))
        val asc = AppServerConfig(configNoReporters)

        // when
        val obj = asc.metricsListener()

        // then
        assertThat(obj).isNull()
    }

    @Test
    fun requestSecurityValidator_returns_a_BasicAuthSecurityValidator() {
        // given
        val asc = AppServerConfig(configForTesting)

        // expect
        assertThat(asc.requestSecurityValidator())
                .isNotNull()
                .isInstanceOf(BasicAuthSecurityValidator::class.java)
    }

    @Test
    fun isDebugActionsEnabled_comes_from_config() {
        // expect
        assertThat(appServerConfig!!.isDebugActionsEnabled)
                .isEqualTo(configForTesting!!.getBoolean("debugActionsEnabled"))
    }

    @Test
    fun endpointsPort_comes_from_config() {
        // expect
        assertThat(appServerConfig!!.endpointsPort()).isEqualTo(configForTesting!!.getInt("endpoints.port"))
    }

    @Test
    fun endpointsSslPort_comes_from_config() {
        // expect
        assertThat(appServerConfig!!.endpointsSslPort()).isEqualTo(configForTesting!!.getInt("endpoints.sslPort"))
    }
}