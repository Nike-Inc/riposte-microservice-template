package com.myorg.ripostemicroservicetemplate.server.config.guice

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Provides
import com.google.inject.name.Names
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID
import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule
import com.nike.internal.util.testing.Glassbox
import com.nike.riposte.metrics.codahale.CodahaleMetricsCollector
import com.nike.riposte.metrics.codahale.CodahaleMetricsEngine
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener
import com.nike.riposte.metrics.codahale.ReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultGraphiteReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultJMXReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultSLF4jReporterFactory
import com.nike.riposte.server.config.AppInfo
import com.nike.riposte.server.config.impl.AppInfoImpl
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil
import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory
import dev.misfitlabs.kotlinguice4.annotatedKey
import dev.misfitlabs.kotlinguice4.getInstance
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.CompletableFuture
import javax.inject.Named
import javax.inject.Singleton

/**
 * Tests the functionality of [AppMetricsGuiceModule].
 */
class AppMetricsGuiceModuleTest {

    private var configForTesting: Config? = null
    private var appGuiceModule: AppMetricsGuiceModule? = null
    private var injector: Injector? = null

    @BeforeEach
    fun beforeMethod() {
        System.setProperty("@appId", APP_ID)
        System.setProperty("@environment", "compiletimetest")
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(
            slf4jReportingEnabled = true,
            jmxReportingEnabled = true,
            graphiteEnabled = false
        )
        appGuiceModule = AppMetricsGuiceModule()
        injector = generateInjector(appGuiceModule, configForTesting)
    }

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

    private fun generateInjector(guiceModule: AppMetricsGuiceModule?, config: Config?): Injector {
        return Guice.createInjector(
            guiceModule,
            TypesafeConfigPropertiesRegistrationGuiceModule(config),
            object : AbstractModule() {
                @Provides
                @Singleton
                @Named("appInfoFuture")
                @Suppress("unused")
                fun appInfoFuture(): CompletableFuture<AppInfo> {
                    return CompletableFuture.completedFuture(AppInfoImpl.createLocalInstance(APP_ID))
                }
            }
        )
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
        assertThat(cmc).isNotNull

        // CodahaleMetricsEngine uses the same CodahaleMetricsCollector and ReporterFactory list, and has been started
        assertThat(engine).isNotNull
        assertThat(Glassbox.getInternalState(engine, "metricsCollector")).isSameAs(cmc)
        assertThat(Glassbox.getInternalState(engine, "reporters") as Collection<ReporterFactory>)
            .containsExactlyInAnyOrderElementsOf(reporters)
        assertThat(Glassbox.getInternalState(engine, "started")).isEqualTo(true)

        // CodahaleMetricsListener uses the same CodahaleMetricsCollector
        assertThat(listener).isNotNull
        assertThat(Glassbox.getInternalState(listener, "metricsCollector")).isSameAs(cmc)
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "true   |   false   |   false",
            "false  |   true    |   false",
            "false  |   false   |   true",
            "true   |   true    |   true",
        ],
        delimiter = '|'
    )
    fun metricsReporters_are_added_as_expected(
        enableSlf4jReporter: Boolean,
        enableJmxReporter: Boolean,
        enableGraphiteReporter: Boolean
    ) {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(
            enableSlf4jReporter, enableJmxReporter, enableGraphiteReporter
        )
        appGuiceModule = AppMetricsGuiceModule()
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
            val expectedPrefix = appInfo.appId() + "." + appInfo.dataCenter() + "." + appInfo.environment() +
                "." + appInfo.instanceId()
            val expectedGraphiteUrl = configForTesting!!.getString("metrics.graphite.url")
            val expectedPort = configForTesting!!.getInt("metrics.graphite.port")
            assertThat(Glassbox.getInternalState(graphiteReporter, "prefix")).isEqualTo(expectedPrefix)
            assertThat(Glassbox.getInternalState(graphiteReporter, "graphiteURL")).isEqualTo(expectedGraphiteUrl)
            assertThat(Glassbox.getInternalState(graphiteReporter, "graphitePort")).isEqualTo(expectedPort)
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
        appGuiceModule = AppMetricsGuiceModule()
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
        assertThat(Glassbox.getInternalState(engine, "reporters") as Collection<ReporterFactory>).isEmpty()
        assertThat(Glassbox.getInternalState(engine, "started")).isEqualTo(true)
    }

    @ParameterizedTest
    @ValueSource(
        booleans = [
            true,
            false,
        ]
    )
    fun codahaleMetricsEngine_is_configured_with_jvm_metrics_on_or_off_based_on_property(reportJvmMetrics: Boolean) {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(
            slf4jReportingEnabled = true,
            jmxReportingEnabled = true,
            graphiteEnabled = false
        ).withValue("metrics.reportJvmMetrics", ConfigValueFactory.fromAnyRef(reportJvmMetrics))
        appGuiceModule = AppMetricsGuiceModule()
        injector = generateInjector(appGuiceModule, configForTesting)

        // when
        val engine = injector!!.getInstance<CodahaleMetricsEngine>()

        // then
        assertThat(Glassbox.getInternalState(engine, "jvmMetricsAdded")).isEqualTo(reportJvmMetrics)
    }
}
