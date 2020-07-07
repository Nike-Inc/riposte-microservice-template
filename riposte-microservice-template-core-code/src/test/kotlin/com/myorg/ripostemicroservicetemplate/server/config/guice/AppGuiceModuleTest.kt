package com.myorg.ripostemicroservicetemplate.server.config.guice

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.name.Names
import com.myorg.ripostemicroservicetemplate.error.ProjectApiErrorsImpl
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.Whitebox
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors
import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule
import com.nike.riposte.client.asynchttp.ning.AsyncHttpClientHelper
import com.nike.riposte.server.config.AppInfo
import com.nike.riposte.server.http.Endpoint
import com.nike.riposte.serviceregistration.eureka.EurekaHandler
import com.nike.riposte.serviceregistration.eureka.EurekaServerHook
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil
import com.tngtech.java.junit.dataprovider.DataProviderRunner
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory
import dev.misfitlabs.kotlinguice4.annotatedKey
import dev.misfitlabs.kotlinguice4.getInstance
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Supplier
import javax.validation.Validator

/**
 * Tests the functionality of [AppGuiceModule]
 *
 * @author Nic Munroe
 */
@RunWith(DataProviderRunner::class)
class AppGuiceModuleTest {

    private var configForTesting: Config? = null
    private var appGuiceModule: AppGuiceModule? = null
    private var injector: Injector? = null

    @Before
    fun beforeMethod() {
        System.setProperty("@appId", APP_ID)
        System.setProperty("@environment", "compiletimetest")
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(
            slf4jReportingEnabled = true,
            jmxReportingEnabled = true,
            graphiteEnabled = false
        )
        appGuiceModule = AppGuiceModule(configForTesting)
        injector = generateInjector(appGuiceModule, configForTesting)
    }

    @Suppress("SameParameterValue")
    private fun generateAppConfigWithMetricsEnabledOrDisabled(
        slf4jReportingEnabled: Boolean,
        jmxReportingEnabled: Boolean,
        graphiteEnabled: Boolean
    ): Config {
        return TypesafeConfigUtil
                .loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest")
                .withValue("metrics.slf4j.reporting.enabled", ConfigValueFactory.fromAnyRef(slf4jReportingEnabled))
                .withValue("metrics.jmx.reporting.enabled", ConfigValueFactory.fromAnyRef(jmxReportingEnabled))
                .withValue("metrics.graphite.reporting.enabled", ConfigValueFactory.fromAnyRef(graphiteEnabled))
    }

    private fun generateInjector(guiceModule: AppGuiceModule?, config: Config?): Injector {
        return Guice.createInjector(
                guiceModule,
                TypesafeConfigPropertiesRegistrationGuiceModule(config)
        )
    }

    @Test
    fun constructor_fails_if_passed_null_appConfig() {
        // when
        val thrown = catchThrowable { AppGuiceModule(null) }

        // then
        assertThat(thrown)
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("appConfig")
    }

    @Test
    fun appEndpoints_returns_non_empty_set() {
        val endpointsSet = injector!!.getInstance(
                annotatedKey<Set<@JvmSuppressWildcards Endpoint<*>>>(Names.named("appEndpoints"))
        )
        assertThat(endpointsSet).isNotEmpty
    }

    @Test
    fun validator_returns_non_null_object() {
        val obj = injector!!.getInstance<Validator>()
        assertThat(obj).isNotNull()
    }

    @Test
    fun projectApiErrors_returns_ProjectApiErrorsImpl() {
        val projectApiErrors = injector!!.getInstance<ProjectApiErrors>()
        assertThat(projectApiErrors)
                .isNotNull()
                .isInstanceOf(ProjectApiErrorsImpl::class.java)
    }

    @Test
    fun asyncHttpClientHelper_returns_non_null_object() {
        val obj = injector!!.getInstance<AsyncHttpClientHelper>()
        assertThat(obj).isNotNull()
    }

    @Test
    fun eurekaServerHook_returns_non_null_object() {
        val obj = injector!!.getInstance<EurekaServerHook>()
        assertThat(obj).isNotNull()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun eurekaServerHook_uses_config_for_suppliers() {
        // given
        val configMock: Config = mock()
        val agm = AppGuiceModule(configMock)
        val eurekaServerHook = agm.eurekaServerHook()
        val eurekaHandler = eurekaServerHook.eurekaHandler
        val eurekaIsDisabledPropertySupplier =
                Whitebox.getInternalState(eurekaHandler, "eurekaIsDisabledPropertySupplier") as Supplier<Boolean>
        val datacenterTypePropertySupplier =
                Whitebox.getInternalState(eurekaHandler, "datacenterTypePropertySupplier") as Supplier<String>

        // when
        eurekaIsDisabledPropertySupplier.get()

        // then
        verify(configMock).getBoolean(EurekaHandler.DISABLE_EUREKA_INTEGRATION)

        // and when
        datacenterTypePropertySupplier.get()

        // then
        verify(configMock).getString(EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME)
    }

    @Test
    fun appInfoFuture_returns_non_null_object() {
        // when
        val appInfoFuture = injector!!.getInstance(
                annotatedKey<CompletableFuture<AppInfo>>(Names.named("appInfoFuture"))
        )

        // then
        assertThat(appInfoFuture).isNotNull
        val appInfo = appInfoFuture.get(1, TimeUnit.SECONDS)
        assertThat(appInfo).isNotNull()
    }
}
