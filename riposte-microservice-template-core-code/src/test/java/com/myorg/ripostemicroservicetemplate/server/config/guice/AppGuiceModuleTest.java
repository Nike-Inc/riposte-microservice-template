package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;
import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule;
import com.nike.riposte.client.asynchttp.AsyncHttpClientHelper;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.http.Endpoint;
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.myorg.ripostemicroservicetemplate.error.ProjectApiErrorsImpl;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.validation.Validator;

import static com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests the functionality of {@link AppGuiceModule}
 *
 * @author Nic Munroe
 */
public class AppGuiceModuleTest {

    @SuppressWarnings("FieldCanBeLocal")
    private Config configForTesting;
    @SuppressWarnings("FieldCanBeLocal")
    private AppGuiceModule appGuiceModule;
    private Injector injector;

    @BeforeEach
    public void beforeMethod() {
        System.setProperty("@appId", APP_ID);
        System.setProperty("@environment", "compiletimetest");
        configForTesting = generateAppConfigWithMetricsDisabled();
        appGuiceModule = new AppGuiceModule(configForTesting);
        injector = generateInjector(appGuiceModule, configForTesting);
    }

    private Config generateAppConfigWithMetricsDisabled() {
        return TypesafeConfigUtil
            .loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest")
            .withValue("metrics.slf4j.reporting.enabled", ConfigValueFactory.fromAnyRef(false))
            .withValue("metrics.jmx.reporting.enabled", ConfigValueFactory.fromAnyRef(false))
            .withValue("metrics.graphite.reporting.enabled", ConfigValueFactory.fromAnyRef(false));
    }

    private Injector generateInjector(AppGuiceModule guiceModule, Config config) {
        return Guice.createInjector(
            guiceModule, new TypesafeConfigPropertiesRegistrationGuiceModule(config)
        );
    }

    @Test
    public void constructor_fails_if_passed_null_appConfig() {
        // when
        @SuppressWarnings("ConstantConditions")
        Throwable thrown = catchThrowable(() -> new AppGuiceModule(null));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("appConfig");
    }

    @Test
    public void appEndpoints_returns_non_empty_set() {
        @SuppressWarnings("Convert2Diamond")
        Set<Endpoint<?>> endpointsSet = injector.getInstance(Key.get(new TypeLiteral<Set<Endpoint<?>>>() {},
                                                                     Names.named("appEndpoints")));
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
    public void appInfoFuture_returns_non_null_object() throws Exception {
        // when
        @SuppressWarnings("Convert2Diamond")
        CompletableFuture<AppInfo> appInfoFuture =
            injector.getInstance(Key.get(new TypeLiteral<CompletableFuture<AppInfo>>() {},
                                         Names.named("appInfoFuture")));

        // then
        assertThat(appInfoFuture).isNotNull();
        AppInfo appInfo = appInfoFuture.get(1, TimeUnit.SECONDS);
        assertThat(appInfo).isNotNull();
    }

}