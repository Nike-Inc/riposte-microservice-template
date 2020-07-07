package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule;
import com.nike.riposte.metrics.codahale.CodahaleMetricsCollector;
import com.nike.riposte.metrics.codahale.CodahaleMetricsEngine;
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener;
import com.nike.riposte.metrics.codahale.ReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultGraphiteReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultJMXReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultSLF4jReporterFactory;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.config.impl.AppInfoImpl;
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import static com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the functionality of {@link AppMetricsGuiceModule}.
 *
 * @author Nic Munroe
 */
@RunWith(DataProviderRunner.class)
public class AppMetricsGuiceModuleTest {

    private Config configForTesting;
    private AppMetricsGuiceModule appGuiceModule;
    private Injector injector;

    @Before
    public void beforeMethod() {
        System.setProperty("@appId", APP_ID);
        System.setProperty("@environment", "compiletimetest");
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(true, true, false);
        appGuiceModule = new AppMetricsGuiceModule();
        injector = generateInjector(appGuiceModule, configForTesting);
    }

    private Config generateAppConfigWithMetricsEnabledOrDisabled(
        boolean slf4jReportingEnabled, boolean jmxReportingEnabled, boolean graphiteEnabled
    ) {
        return TypesafeConfigUtil
            .loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest")
            .withValue("metrics.slf4j.reporting.enabled", ConfigValueFactory.fromAnyRef(slf4jReportingEnabled))
            .withValue("metrics.jmx.reporting.enabled", ConfigValueFactory.fromAnyRef(jmxReportingEnabled))
            .withValue("metrics.graphite.reporting.enabled", ConfigValueFactory.fromAnyRef(graphiteEnabled));
    }

    private Injector generateInjector(AppMetricsGuiceModule guiceModule, Config config) {
        return Guice.createInjector(
            guiceModule,
            new TypesafeConfigPropertiesRegistrationGuiceModule(config),
            new AbstractModule() {
                @Provides @Singleton @Named("appInfoFuture")
                @SuppressWarnings("unused")
                public CompletableFuture<AppInfo> appInfoFuture() {
                    return CompletableFuture.completedFuture(AppInfoImpl.createLocalInstance(APP_ID));
                }
            }
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void metrics_related_objects_are_non_null_and_related_to_each_other_as_expected() {
        // when
        List<ReporterFactory> reporters = injector.getInstance(Key.get(new TypeLiteral<List<ReporterFactory>>() {}));
        CodahaleMetricsCollector cmc = injector.getInstance(CodahaleMetricsCollector.class);
        CodahaleMetricsEngine engine = injector.getInstance(CodahaleMetricsEngine.class);
        CodahaleMetricsListener listener = injector.getInstance(CodahaleMetricsListener.class);

        // then
        // ReporterFactory list is not empty
        assertThat(reporters).isNotNull();
        assertThat(reporters).isNotEmpty();

        // CodahaleMetricsCollector exists
        assertThat(cmc).isNotNull();

        // CodahaleMetricsEngine uses the same CodahaleMetricsCollector and ReporterFactory list, and has been started
        assertThat(engine).isNotNull();
        assertThat(TestUtils.Whitebox.getInternalState(engine, "metricsCollector")).isSameAs(cmc);
        assertThat((Collection<ReporterFactory>) TestUtils.Whitebox.getInternalState(engine, "reporters"))
            .containsOnlyElementsOf(reporters);
        assertThat(TestUtils.Whitebox.getInternalState(engine, "started")).isEqualTo(true);

        // CodahaleMetricsListener uses the same CodahaleMetricsCollector
        assertThat(listener).isNotNull();
        assertThat(TestUtils.Whitebox.getInternalState(listener, "metricsCollector")).isSameAs(cmc);
    }

    @DataProvider(value = {
        "true   |   false   |   false",
        "false  |   true    |   false",
        "false  |   false   |   true",
        "true   |   true    |   true",
    }, splitBy = "\\|")
    @Test
    public void metricsReporters_are_added_as_expected(boolean enableSlf4jReporter, boolean enableJmxReporter,
                                                       boolean enableGraphiteReporter) {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(enableSlf4jReporter, enableJmxReporter,
                                                                         enableGraphiteReporter);
        appGuiceModule = new AppMetricsGuiceModule();
        injector = generateInjector(appGuiceModule, configForTesting);

        // when
        List<ReporterFactory> reporters = injector.getInstance(Key.get(new TypeLiteral<List<ReporterFactory>>() {}));
        List<Class<? extends ReporterFactory>> reporterClasses = reporters.stream()
                                                                          .map(ReporterFactory::getClass)
                                                                          .collect(Collectors.toList());

        // then
        if (enableSlf4jReporter) {
            assertThat(reporterClasses).contains(DefaultSLF4jReporterFactory.class);
        }
        else {
            assertThat(reporterClasses).doesNotContain(DefaultSLF4jReporterFactory.class);
        }

        if (enableJmxReporter) {
            assertThat(reporterClasses).contains(DefaultJMXReporterFactory.class);
        }
        else {
            assertThat(reporterClasses).doesNotContain(DefaultJMXReporterFactory.class);
        }

        if (enableGraphiteReporter) {
            assertThat(reporterClasses).contains(DefaultGraphiteReporterFactory.class);
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            DefaultGraphiteReporterFactory graphiteReporter = (DefaultGraphiteReporterFactory)reporters
                .stream().filter(r -> r instanceof DefaultGraphiteReporterFactory).findFirst().get();
            AppInfo appInfo = injector.getInstance(Key.get(new TypeLiteral<CompletableFuture<AppInfo>>() {},
                                                           Names.named("appInfoFuture"))).join();
            String expectedPrefix = appInfo.appId() + "." + appInfo.dataCenter() + "." + appInfo.environment()
                                    + "." + appInfo.instanceId();
            String expectedGraphiteUrl = configForTesting.getString("metrics.graphite.url");
            int expectedPort = configForTesting.getInt("metrics.graphite.port");
            assertThat(TestUtils.Whitebox.getInternalState(graphiteReporter, "prefix")).isEqualTo(expectedPrefix);
            assertThat(
                TestUtils.Whitebox.getInternalState(graphiteReporter, "graphiteURL")).isEqualTo(expectedGraphiteUrl);
            assertThat(TestUtils.Whitebox.getInternalState(graphiteReporter, "graphitePort")).isEqualTo(expectedPort);
        }
        else {
            assertThat(reporterClasses).doesNotContain(DefaultGraphiteReporterFactory.class);
        }
    }

    @Test
    public void metrics_related_objects_are_null_if_all_reporters_are_disabled() {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(false, false, false);
        appGuiceModule = new AppMetricsGuiceModule();
        injector = generateInjector(appGuiceModule, configForTesting);

        // when
        List<ReporterFactory> reporters = injector.getInstance(Key.get(new TypeLiteral<List<ReporterFactory>>() {}));
        CodahaleMetricsCollector cmc = injector.getInstance(CodahaleMetricsCollector.class);
        CodahaleMetricsEngine engine = injector.getInstance(CodahaleMetricsEngine.class);
        CodahaleMetricsListener listener = injector.getInstance(CodahaleMetricsListener.class);

        // then
        assertThat(reporters).isNull();
        assertThat(cmc).isNull();
        assertThat(engine).isNull();
        assertThat(listener).isNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void codahaleMetricsEngine_gracefully_handles_null_reporters_list() {
        // given
        CodahaleMetricsCollector cmc = new CodahaleMetricsCollector();

        // when
        CodahaleMetricsEngine engine = appGuiceModule.codahaleMetricsEngine(cmc, null, false);

        // then
        assertThat((Collection<ReporterFactory>) TestUtils.Whitebox.getInternalState(engine, "reporters")).isEmpty();
        assertThat(TestUtils.Whitebox.getInternalState(engine, "started")).isEqualTo(true);
    }

    @DataProvider(value = {
        "true",
        "false"
    })
    @Test
    public void codahaleMetricsEngine_is_configured_with_jvm_metrics_on_or_off_based_on_property(boolean reportJvmMetrics) {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(true, true, false)
            .withValue("metrics.reportJvmMetrics", ConfigValueFactory.fromAnyRef(reportJvmMetrics));
        appGuiceModule = new AppMetricsGuiceModule();
        injector = generateInjector(appGuiceModule, configForTesting);

        // when
        CodahaleMetricsEngine engine = injector.getInstance(CodahaleMetricsEngine.class);

        // then
        assertThat(TestUtils.Whitebox.getInternalState(engine, "jvmMetricsAdded")).isEqualTo(reportJvmMetrics);
    }
}