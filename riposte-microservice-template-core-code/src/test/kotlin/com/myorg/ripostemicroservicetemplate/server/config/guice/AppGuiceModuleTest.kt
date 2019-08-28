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
import com.nike.riposte.metrics.codahale.CodahaleMetricsCollector
import com.nike.riposte.metrics.codahale.CodahaleMetricsEngine
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener
import com.nike.riposte.metrics.codahale.ReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultGraphiteReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultJMXReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultSLF4jReporterFactory
import com.nike.riposte.server.config.AppInfo
import com.nike.riposte.server.http.Endpoint
import com.nike.riposte.serviceregistration.eureka.EurekaHandler
import com.nike.riposte.serviceregistration.eureka.EurekaServerHook
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil
import com.tngtech.java.junit.dataprovider.DataProvider
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

    private fun generateAppConfigWithMetricsEnabledOrDisabled(
            slf4jReportingEnabled: Boolean, jmxReportingEnabled: Boolean, graphiteEnabled: Boolean
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
    @Suppress("UNCHECKED_CAST")
    fun metrics_related_objects_are_non_null_and_related_to_each_other_as_expected() {
        // when
        val reporters = injector!!.getInstance<List<@JvmSuppressWildcards ReporterFactory>>()
        val cmc = injector!!.getInstance<CodahaleMetricsCollector>()
        val engine = injector!!.getInstance<CodahaleMetricsEngine>()
        val listener = injector!!.getInstance<CodahaleMetricsListener>()

        // then
        // ReporterFactory list is not empty
        assertThat(reporters).isNotNull
        assertThat(reporters).isNotEmpty

        // CodahaleMetricsCollector exists
        assertThat(cmc).isNotNull()

        // CodahaleMetricsEngine uses the same CodahaleMetricsCollector and ReporterFactory list, and has been started
        assertThat(engine).isNotNull()
        assertThat(Whitebox.getInternalState(engine, "metricsCollector")).isSameAs(cmc)
        assertThat(Whitebox.getInternalState(engine, "reporters") as Collection<ReporterFactory>)
                .containsOnlyElementsOf(reporters)
        assertThat(Whitebox.getInternalState(engine, "started")).isEqualTo(true)

        // CodahaleMetricsListener uses the same CodahaleMetricsCollector
        assertThat(listener).isNotNull()
        assertThat(Whitebox.getInternalState(listener, "metricsCollector")).isSameAs(cmc)
    }

    @DataProvider(value = [
        "true   |   false   |   false",
        "false  |   true    |   false",
        "false  |   false   |   true",
        "true   |   true    |   true"
    ], splitBy = "\\|")
    @Test
    fun metricsReporters_are_added_as_expected(enableSlf4jReporter: Boolean, enableJmxReporter: Boolean,
                                               enableGraphiteReporter: Boolean) {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(
                enableSlf4jReporter, enableJmxReporter, enableGraphiteReporter
        )
        appGuiceModule = AppGuiceModule(configForTesting)
        injector = generateInjector(appGuiceModule, configForTesting)

        // when
        val reporters = injector!!.getInstance<List<@JvmSuppressWildcards ReporterFactory>>()
        val reporterClasses: List<Class<*>> = reporters.map { it.javaClass }

        // then
        if (enableSlf4jReporter)
            assertThat(reporterClasses).contains(DefaultSLF4jReporterFactory::class.java)
        else
            assertThat(reporterClasses).doesNotContain(DefaultSLF4jReporterFactory::class.java)

        if (enableJmxReporter)
            assertThat(reporterClasses).contains(DefaultJMXReporterFactory::class.java)
        else
            assertThat(reporterClasses).doesNotContain(DefaultJMXReporterFactory::class.java)

        if (enableGraphiteReporter) {
            assertThat(reporterClasses).contains(DefaultGraphiteReporterFactory::class.java)
            val graphiteReporter = reporters.first { r -> r is DefaultGraphiteReporterFactory }
            val appInfo = injector!!.getInstance(
                    annotatedKey<CompletableFuture<AppInfo>>(Names.named("appInfoFuture"))
            ).join()
            val expectedPrefix = (appInfo.appId() + "." + appInfo.dataCenter() + "." + appInfo.environment()
                    + "." + appInfo.instanceId())
            val expectedGraphiteUrl = configForTesting!!.getString("metrics.graphite.url")
            val expectedPort = configForTesting!!.getInt("metrics.graphite.port")
            assertThat(Whitebox.getInternalState(graphiteReporter, "prefix")).isEqualTo(expectedPrefix)
            assertThat(Whitebox.getInternalState(graphiteReporter, "graphiteURL")).isEqualTo(expectedGraphiteUrl)
            assertThat(Whitebox.getInternalState(graphiteReporter, "graphitePort")).isEqualTo(expectedPort)
        } else
            assertThat(reporterClasses).doesNotContain(DefaultGraphiteReporterFactory::class.java)
    }

    @Test
    fun metrics_related_objects_are_null_if_all_reporters_are_disabled() {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(
            slf4jReportingEnabled = false,
            jmxReportingEnabled = false,
            graphiteEnabled = false
        )
        appGuiceModule = AppGuiceModule(configForTesting)
        injector = generateInjector(appGuiceModule, configForTesting)

        // when
        val reporters = injector!!.getInstance<List<@JvmSuppressWildcards ReporterFactory>>()
        val cmc = injector!!.getInstance<CodahaleMetricsCollector>()
        val engine = injector!!.getInstance<CodahaleMetricsEngine>()
        val listener = injector!!.getInstance<CodahaleMetricsListener>()

        // then
        assertThat(reporters).isNull()
        assertThat(cmc).isNull()
        assertThat(engine).isNull()
        assertThat(listener).isNull()
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun codahaleMetricsEngine_gracefully_handles_null_reporters_list() {
        // given
        val cmc = CodahaleMetricsCollector()

        // when
        val engine: CodahaleMetricsEngine = appGuiceModule!!.codahaleMetricsEngine(cmc, null, false)!!

        // then
        assertThat(Whitebox.getInternalState(engine, "reporters") as Collection<ReporterFactory>).isEmpty()
        assertThat(Whitebox.getInternalState(engine, "started")).isEqualTo(true)
    }

    @DataProvider(value = [
        "true",
        "false"
    ])
    @Test
    fun codahaleMetricsEngine_is_configured_with_jvm_metrics_on_or_off_based_on_property(reportJvmMetrics: Boolean) {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(
            slf4jReportingEnabled = true,
            jmxReportingEnabled = true,
            graphiteEnabled = false
        ).withValue("metrics.reportJvmMetrics", ConfigValueFactory.fromAnyRef(reportJvmMetrics))
        appGuiceModule = AppGuiceModule(configForTesting)
        injector = generateInjector(appGuiceModule, configForTesting)

        // when
        val engine = injector!!.getInstance<CodahaleMetricsEngine>()

        // then
        assertThat(Whitebox.getInternalState(engine, "jvmMetricsAdded")).isEqualTo(reportJvmMetrics)
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