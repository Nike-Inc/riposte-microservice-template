package com.myorg.ripostemicroservicetemplate.server.config;

import com.nike.backstopper.handler.riposte.RiposteApiExceptionHandler;
import com.nike.backstopper.handler.riposte.RiposteUnhandledExceptionHandler;
import com.nike.backstopper.handler.riposte.config.guice.BackstopperRiposteConfigGuiceModule;
import com.nike.backstopper.service.riposte.BackstopperRiposteValidatorAdapter;
import com.nike.riposte.metrics.MetricsListener;
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener;
import com.nike.riposte.metrics.codahale.EndpointMetricsHandler;
import com.nike.riposte.metrics.codahale.impl.EndpointMetricsHandlerDefaultImpl;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator;
import com.nike.riposte.server.logging.AccessLogger;
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil;

import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import com.google.inject.util.Providers;
import com.myorg.ripostemicroservicetemplate.server.config.guice.AppGuiceModule;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.myorg.ripostemicroservicetemplate.testutils.TestUtils.APP_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Tests the functionality of {@link AppServerConfig}
 *
 * @author Nic Munroe
 */
public class AppServerConfigTest {

    private Config configForTesting;
    private AppServerConfig appServerConfig;

    @Before
    public void beforeMethod() {
        System.setProperty("@appId", APP_ID);
        System.setProperty("@environment", "compiletimetest");
        configForTesting = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest");
        appServerConfig = new AppServerConfig(configForTesting);
    }

    @Test
    public void constructor_fails_if_passed_null_appConfig() {
        // when
        Throwable thrown = catchThrowable(() -> new AppServerConfig(null));

        // then
        assertThat(thrown)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("appConfig");
    }

    @Test
    public void constructor_calls_initEndpointAndServerConfigMetrics_on_metricsListener() {
        // given
        AppServerConfig asc = new AppServerConfig(configForTesting);

        // expect
        EndpointMetricsHandler emh = ((CodahaleMetricsListener) asc.metricsListener()).getEndpointMetricsHandler();
        assertThat(emh).isInstanceOf(EndpointMetricsHandlerDefaultImpl.class);
        assertThat(((EndpointMetricsHandlerDefaultImpl)emh).getEndpointRequestsTimers()).isNotEmpty();
    }

    @Test
    public void constructor_does_not_blow_up_if_metricsListener_is_null() {
        // given
        AppServerConfig asc = new AppServerConfig(configForTesting) {
            @Override
            protected List<Module> getAppGuiceModules(Config appConfig) {
                return Arrays.asList(
                    Modules.override(new AppGuiceModule(appConfig)).with(
                        binder -> binder
                            .bind(new TypeLiteral<CodahaleMetricsListener>() {})
                            .toProvider(Providers.of(null))),
                    new BackstopperRiposteConfigGuiceModule()
                );
            }
        };

        // expect
        assertThat(asc.metricsListener()).isNull();
    }

    @Test
    public void an_BackstopperRiposteConfigGuiceModule_is_used_to_setup_error_handlers_and_validators() {
        // expect
        assertThat(appServerConfig.riposteErrorHandler()).isInstanceOf(RiposteApiExceptionHandler.class);
        assertThat(appServerConfig.riposteUnhandledErrorHandler()).isInstanceOf(RiposteUnhandledExceptionHandler.class);
        assertThat(appServerConfig.requestContentValidationService())
            .isInstanceOf(BackstopperRiposteValidatorAdapter.class);
    }

    @Test
    public void accessLogger_returns_non_null_object() {
        // when
        AccessLogger obj = appServerConfig.accessLogger();

        // then
        assertThat(obj).isNotNull();
    }

    @Test
    public void appInfo_returns_non_null_object() {
        // when
        CompletableFuture<AppInfo> obj = appServerConfig.appInfo();

        // then
        assertThat(obj).isNotNull();
        assertThat(obj.join()).isNotNull();
    }

    @Test
    public void metricsListener_returns_non_null_object() {
        // when
        MetricsListener obj = appServerConfig.metricsListener();

        // then
        assertThat(obj).isNotNull();
    }

    @Test
    public void metricsListener_returns_null_object_if_no_metrics_reporters_are_enabled() {
        // given
        Config configNoReporters =
            TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(APP_ID, "compiletimetest")
                              .withValue("metrics.slf4j.reporting.enabled", ConfigValueFactory.fromAnyRef(false))
                              .withValue("metrics.graphite.reporting.enabled", ConfigValueFactory.fromAnyRef(false))
                              .withValue("metrics.jmx.reporting.enabled", ConfigValueFactory.fromAnyRef(false));
        AppServerConfig asc = new AppServerConfig(configNoReporters);

        // when
        MetricsListener obj = asc.metricsListener();

        // then
        assertThat(obj).isNull();
    }

    @Test
    public void requestSecurityValidator_returns_a_BasicAuthSecurityValidator() {
        // given
        AppServerConfig asc = new AppServerConfig(configForTesting);

        // expect
        assertThat(asc.requestSecurityValidator())
            .isNotNull()
            .isInstanceOf(BasicAuthSecurityValidator.class);
    }

    @Test
    public void isDebugActionsEnabled_comes_from_config() {
        // expect
        assertThat(appServerConfig.isDebugActionsEnabled())
            .isEqualTo(configForTesting.getBoolean("debugActionsEnabled"));
    }

    @Test
    public void endpointsPort_comes_from_config() {
        // expect
        assertThat(appServerConfig.endpointsPort()).isEqualTo(configForTesting.getInt("endpoints.port"));
    }

    @Test
    public void endpointsSslPort_comes_from_config() {
        // expect
        assertThat(appServerConfig.endpointsSslPort()).isEqualTo(configForTesting.getInt("endpoints.sslPort"));
    }
}