package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;
import com.nike.riposte.client.asynchttp.ning.AsyncHttpClientHelper;
import com.nike.riposte.metrics.codahale.CodahaleMetricsCollector;
import com.nike.riposte.metrics.codahale.CodahaleMetricsEngine;
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener;
import com.nike.riposte.metrics.codahale.ReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultGraphiteReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultJMXReporterFactory;
import com.nike.riposte.metrics.codahale.contrib.DefaultSLF4jReporterFactory;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator;
import com.nike.riposte.server.http.Endpoint;
import com.nike.riposte.serviceregistration.eureka.EurekaHandler;
import com.nike.riposte.serviceregistration.eureka.EurekaServerHook;
import com.nike.riposte.util.AwsUtil;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleCassandraAsyncEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleDownstreamHttpAsyncEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleProxyRouterEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.HealthCheckEndpoint;
import com.myorg.ripostemicroservicetemplate.error.ProjectApiErrorsImpl;
import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * The main Guice module for the application. The {@link #validator()}, {@link #projectApiErrors()}, {@link
 * #appInfoFuture(AsyncHttpClientHelper)}, and {@code appEndpoints(...)} methods are required for the application to
 * function at a basic level. {@link #eurekaServerHook()}, metrics related methods, and {@link
 * #basicAuthSecurityValidator(List, String, String)} are used to enable Eureka registration, metrics gathering and
 * reporting, and basic auth endpoint protection features respectively.
 *
 * <p>You can put anything else in this class that you want to be available for injection in your {@link Endpoint}s. As
 * you add endpoints just add them to the {@code appEndpoints(...)} argument list and return them from that method.
 * Injection will work for those endpoint classes and they will be auto-registered with the application and ready to
 * accept requests.
 *
 * <p>NOTE: See the implementation of {@link #basicAuthProtectedEndpoints(Set)} when you're done with the example
 * endpoints and are ready to create your application's real endpoints. You may want to follow the inline comment
 * instructions in that method to enable protection of all non-healthcheck endpoints. Alternately you can choose only
 * specific endpoints to protect, or disable basic auth entirely, or switch to a different auth scheme, or remove
 * security validation entirely depending on your application's requirements.
 *
 * @author Nic Munroe
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AppGuiceModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Config appConfig;

    public AppGuiceModule(Config appConfig) {
        if (appConfig == null)
            throw new IllegalArgumentException("appConfig cannot be null");

        this.appConfig = appConfig;
    }

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    @Named("appEndpoints")
    public Set<Endpoint<?>> appEndpoints(
        HealthCheckEndpoint healthCheckEndpoint,
        // TODO: EXAMPLE CLEANUP - Remove these example endpoints from this method's arguments and don't return them from this method.
        ExampleEndpoint.Get exampleEndpointGet,
        ExampleEndpoint.Post exampleEndpointPost,
        ExampleCassandraAsyncEndpoint exampleCassandraAsyncEndpoint,
        ExampleDownstreamHttpAsyncEndpoint exampleDownstreamHttpAsyncEndpoint,
        ExampleProxyRouterEndpoint exampleProxyRouterEndpoint,
        ExampleBasicAuthProtectedEndpoint.Get exampleBasicAuthProtectedEndpointGet,
        ExampleBasicAuthProtectedEndpoint.Post exampleBasicAuthProtectedEndpointPost
    ) {
        return new LinkedHashSet<>(Arrays.<Endpoint<?>>asList(
            healthCheckEndpoint,
            // Example endpoints
            exampleEndpointGet, exampleEndpointPost, exampleCassandraAsyncEndpoint,
            exampleDownstreamHttpAsyncEndpoint, exampleProxyRouterEndpoint,
            exampleBasicAuthProtectedEndpointGet, exampleBasicAuthProtectedEndpointPost
        ));
    }

    @Provides
    @Singleton
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Provides
    @Singleton
    public ProjectApiErrors projectApiErrors() {
        return new ProjectApiErrorsImpl();
    }

    @Provides
    @Singleton
    public AsyncHttpClientHelper asyncHttpClientHelper() {
        return new AsyncHttpClientHelper();
    }

    @Provides
    @Singleton
    public EurekaServerHook eurekaServerHook() {
        return new EurekaServerHook(
            () -> appConfig.getBoolean(EurekaHandler.DISABLE_EUREKA_INTEGRATION),
            () -> appConfig.getString(EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME)
        );
    }

    @Provides
    @Singleton
    public CodahaleMetricsListener metricsListener(@Nullable CodahaleMetricsCollector metricsCollector,
                                                   @Nullable CodahaleMetricsEngine engine) {
        if (metricsCollector == null)
            return null;

        // We don't actually need the CodahaleMetricsEngine, but we ask for it here to guarantee that it is created and
        //      started.

        return new CodahaleMetricsListener(metricsCollector);
    }

    @Provides
    @Singleton
    public CodahaleMetricsEngine codahaleMetricsEngine(@Nullable CodahaleMetricsCollector cmc,
                                                       @Nullable List<ReporterFactory> reporters,
                                                       @Named("metrics.reportJvmMetrics") boolean reportJvmMetrics) {
        if (cmc == null)
            return null;

        if (reporters == null)
            reporters = Collections.emptyList();

        CodahaleMetricsEngine engine = new CodahaleMetricsEngine(cmc, reporters);
        if (reportJvmMetrics)
            engine.reportJvmMetrics();
        engine.start();
        return engine;
    }

    @Provides
    @Singleton
    public CodahaleMetricsCollector codahaleMetricsCollector(@Nullable List<ReporterFactory> reporters) {
        if (reporters == null)
            return null;

        return new CodahaleMetricsCollector();
    }

    @Provides
    @Singleton
    public List<ReporterFactory> metricsReporters(
        @Named("metrics.slf4j.reporting.enabled") boolean slf4jReportingEnabled,
        @Named("metrics.jmx.reporting.enabled") boolean jmxReportingEnabled,
        @Named("metrics.graphite.url") String graphiteUrl,
        @Named("metrics.graphite.port") int graphitePort,
        @Named("metrics.graphite.reporting.enabled") boolean graphiteEnabled,
        @Named("appInfoFuture") CompletableFuture<AppInfo> appInfoFuture
    ) {
        List<ReporterFactory> reporters = new ArrayList<>();

        if (slf4jReportingEnabled)
            reporters.add(new DefaultSLF4jReporterFactory());

        if (jmxReportingEnabled)
            reporters.add(new DefaultJMXReporterFactory());

        if (graphiteEnabled) {
            AppInfo appInfo = appInfoFuture.join();
            String graphitePrefix = appInfo.appId() + "." + appInfo.dataCenter() + "." + appInfo.environment()
                                    + "." + appInfo.instanceId();
            reporters.add(new DefaultGraphiteReporterFactory(graphitePrefix, graphiteUrl, graphitePort));
        }

        if (reporters.isEmpty()) {
            logger.info("No metrics reporters enabled - disabling metrics entirely.");
            return null;
        }

        String metricReporterTypes = reporters.stream()
                                              .map(rf -> rf.getClass().getSimpleName())
                                              .collect(Collectors.joining(",", "[", "]"));
        logger.info("Metrics reporters enabled. metric_reporter_types={}", metricReporterTypes);

        return reporters;
    }

    @Provides
    @Singleton
    @Named("basicAuthProtectedEndpoints")
    public List<Endpoint<?>> basicAuthProtectedEndpoints(@Named("appEndpoints") Set<Endpoint<?>> endpoints) {
        // TODO: EXAMPLE CLEANUP - Think about what you want this method to do and adjust it accordingly.
        // For most projects you probably want everything except the healthcheck endpoint.
        //      That's the commented-out line below.
//        return endpoints.stream().filter(i -> !(i instanceof HealthCheckEndpoint)).collect(Collectors.toList());

        // For the example template though, we only want the ExampleBasicAuthProtectedEndpoint.Post class protected and
        //      everything else should be allowed. Once you're done with the examples and ready to build your app you
        //      may want to delete the line below and uncomment the line above, or otherwise adjust the filter for your
        //      needs.
        return endpoints.stream().filter(i -> (i instanceof ExampleBasicAuthProtectedEndpoint.Post))
                        .collect(Collectors.toList());
    }

    @Provides
    @Singleton
    public BasicAuthSecurityValidator basicAuthSecurityValidator(
        @Named("basicAuthProtectedEndpoints") List<Endpoint<?>> basicAuthProtectedEndpoints,
        @Named("exampleBasicAuth.username") String basicAuthUsername,
        @Named("exampleBasicAuth.password") String basicAuthPassword
    ) {
        return new BasicAuthSecurityValidator(basicAuthProtectedEndpoints, basicAuthUsername, basicAuthPassword);
    }

    @Provides
    @Singleton
    @Named("appInfoFuture")
    public CompletableFuture<AppInfo> appInfoFuture(AsyncHttpClientHelper asyncHttpClientHelper) {
        return AwsUtil.getAppInfoFutureWithAwsInfo(asyncHttpClientHelper);
    }
}
