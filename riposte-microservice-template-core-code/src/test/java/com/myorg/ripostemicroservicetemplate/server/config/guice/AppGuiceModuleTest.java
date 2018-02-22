package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;
import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule;
import com.nike.riposte.client.asynchttp.ning.AsyncHttpClientHelper;
import com.nike.riposte.metrics.codahale.CodahaleMetricsCollector;
import com.nike.riposte.metrics.codahale.CodahaleMetricsEngine;
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener;
import com.nike.riposte.metrics.codahale.ReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultGraphiteReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultJMXReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultSLF4jReporterFactory;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.http.Endpoint;
import com.nike.riposte.serviceregistration.eureka.EurekaHandler;
import com.nike.riposte.serviceregistration.eureka.EurekaServerHook;
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.myorg.ripostemicroservicetemplate.error.ProjectApiErrorsImpl;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.reflection.Whitebox;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.validation.Validator;

import static com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests the functionality of {@link AppGuiceModule}
 *
 * @author Nic Munroe
 */
@RunWith(DataProviderRunner.class)
public class AppGuiceModuleTest {

    private Config configForTesting;
    private AppGuiceModule appGuiceModule;
    private Injector injector;

    @Before
    public void beforeMethod() {
        System.setProperty("@appId", APP_ID);
        System.setProperty("@environment", "compiletimetest");
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(true, true, false);
        appGuiceModule = new AppGuiceModule(configForTesting);
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

    private Injector generateInjector(AppGuiceModule guiceModule, Config config) {
        return Guice.createInjector(
            guiceModule, new TypesafeConfigPropertiesRegistrationGuiceModule(config)
        );
    }

    @Test
    public void constructor_fails_if_passed_null_appConfig() {
        // when
        Throwable thrown = catchThrowable(() -> new AppGuiceModule(null));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("appConfig");
    }

    @Test
    public void appEndpoints_returns_non_empty_set() {
        Set<Endpoint<?>> endpointsSet = injector.getInstance(Key.get(new TypeLiteral<Set<Endpoint<?>>>() {
        }, Names.named("appEndpoints")));
        assertThat(endpointsSet).isNotEmpty();
    }

    @Test
    public void validator_returns_non_null_object() {
        Validator obj = injector.getInstance(Validator.class);
        assertThat(obj).isNotNull();
    }

    @Test
    public void projectApiErrors_returns_ProjectApiErrorsImpl() {
        ProjectApiErrors projectApiErrors = injector.getInstance(ProjectApiErrors.class);
        assertThat(projectApiErrors)
            .isNotNull()
            .isInstanceOf(ProjectApiErrorsImpl.class);
    }

    @Test
    public void asyncHttpClientHelper_returns_non_null_object() {
        AsyncHttpClientHelper obj = injector.getInstance(AsyncHttpClientHelper.class);
        assertThat(obj).isNotNull();
    }

    @Test
    public void eurekaServerHook_returns_non_null_object() {
        EurekaServerHook obj = injector.getInstance(EurekaServerHook.class);
        assertThat(obj).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void eurekaServerHook_uses_config_for_suppliers() {
        // given
        Config configMock = mock(Config.class);
        AppGuiceModule agm = new AppGuiceModule(configMock);
        EurekaServerHook eurekaServerHook = agm.eurekaServerHook();
        EurekaHandler eurekaHandler = eurekaServerHook.eurekaHandler;
        Supplier<Boolean> eurekaIsDisabledPropertySupplier =
            (Supplier<Boolean>) Whitebox.getInternalState(eurekaHandler, "eurekaIsDisabledPropertySupplier");
        Supplier<String> datacenterTypePropertySupplier =
            (Supplier<String>) Whitebox.getInternalState(eurekaHandler, "datacenterTypePropertySupplier");

        // when
        eurekaIsDisabledPropertySupplier.get();

        // then
        verify(configMock).getBoolean(EurekaHandler.DISABLE_EUREKA_INTEGRATION);

        // and when
        datacenterTypePropertySupplier.get();

        // then
        verify(configMock).getString(EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME);
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
        assertThat(Whitebox.getInternalState(engine, "metricsCollector")).isSameAs(cmc);
        assertThat((Collection<ReporterFactory>)Whitebox.getInternalState(engine, "reporters"))
            .containsOnlyElementsOf(reporters);
        assertThat(Whitebox.getInternalState(engine, "started")).isEqualTo(true);

        // CodahaleMetricsListener uses the same CodahaleMetricsCollector
        assertThat(listener).isNotNull();
        assertThat(Whitebox.getInternalState(listener, "metricsCollector")).isSameAs(cmc);
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
        appGuiceModule = new AppGuiceModule(configForTesting);
        injector = generateInjector(appGuiceModule, configForTesting);

        // when
        List<ReporterFactory> reporters = injector.getInstance(Key.get(new TypeLiteral<List<ReporterFactory>>() {}));
        List<Class<? extends ReporterFactory>> reporterClasses = reporters.stream()
                                                                .map(ReporterFactory::getClass)
                                                                .collect(Collectors.toList());

        // then
        if (enableSlf4jReporter)
            assertThat(reporterClasses).contains(DefaultSLF4jReporterFactory.class);
        else
            assertThat(reporterClasses).doesNotContain(DefaultSLF4jReporterFactory.class);

        if (enableJmxReporter)
            assertThat(reporterClasses).contains(DefaultJMXReporterFactory.class);
        else
            assertThat(reporterClasses).doesNotContain(DefaultJMXReporterFactory.class);

        if (enableGraphiteReporter) {
            assertThat(reporterClasses).contains(DefaultGraphiteReporterFactory.class);
            @SuppressWarnings("ConstantConditions")
            DefaultGraphiteReporterFactory graphiteReporter = (DefaultGraphiteReporterFactory)reporters
                .stream().filter(r -> r instanceof DefaultGraphiteReporterFactory).findFirst().get();
            AppInfo appInfo = injector.getInstance(Key.get(new TypeLiteral<CompletableFuture<AppInfo>>() {},
                                                           Names.named("appInfoFuture"))).join();
            String expectedPrefix = appInfo.appId() + "." + appInfo.dataCenter() + "." + appInfo.environment()
                                    + "." + appInfo.instanceId();
            String expectedGraphiteUrl = configForTesting.getString("metrics.graphite.url");
            int expectedPort = configForTesting.getInt("metrics.graphite.port");
            assertThat(Whitebox.getInternalState(graphiteReporter, "prefix")).isEqualTo(expectedPrefix);
            assertThat(Whitebox.getInternalState(graphiteReporter, "graphiteURL")).isEqualTo(expectedGraphiteUrl);
            assertThat(Whitebox.getInternalState(graphiteReporter, "graphitePort")).isEqualTo(expectedPort);
        }
        else
            assertThat(reporterClasses).doesNotContain(DefaultGraphiteReporterFactory.class);
    }

    @Test
    public void metrics_related_objects_are_null_if_all_reporters_are_disabled() {
        // given
        configForTesting = generateAppConfigWithMetricsEnabledOrDisabled(false, false, false);
        appGuiceModule = new AppGuiceModule(configForTesting);
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
        assertThat((Collection<ReporterFactory>)Whitebox.getInternalState(engine, "reporters")).isEmpty();
        assertThat(Whitebox.getInternalState(engine, "started")).isEqualTo(true);
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
        appGuiceModule = new AppGuiceModule(configForTesting);
        injector = generateInjector(appGuiceModule, configForTesting);

        // when
        CodahaleMetricsEngine engine = injector.getInstance(CodahaleMetricsEngine.class);

        // then
        assertThat(Whitebox.getInternalState(engine, "jvmMetricsAdded")).isEqualTo(reportJvmMetrics);
    }

    @Test
    public void appInfoFuture_returns_non_null_object() throws Exception {
        // when
        CompletableFuture<AppInfo> appInfoFuture =
            injector.getInstance(Key.get(new TypeLiteral<CompletableFuture<AppInfo>>() {},
                                         Names.named("appInfoFuture")));

        // then
        assertThat(appInfoFuture).isNotNull();
        AppInfo appInfo = appInfoFuture.get(1, TimeUnit.SECONDS);
        assertThat(appInfo).isNotNull();
    }

}