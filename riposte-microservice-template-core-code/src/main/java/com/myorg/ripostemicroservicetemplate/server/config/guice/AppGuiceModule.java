package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;
import com.nike.riposte.client.asynchttp.AsyncHttpClientHelper;
import com.nike.riposte.client.asynchttp.util.AwsUtil;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.http.Endpoint;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleDownstreamHttpAsyncEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleProxyRouterEndpoint;
import com.myorg.ripostemicroservicetemplate.endpoints.HealthCheckEndpoint;
import com.myorg.ripostemicroservicetemplate.error.ProjectApiErrorsImpl;
import com.typesafe.config.Config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * The main Guice module for the application. The {@link #validator()}, {@link #projectApiErrors()}, {@link
 * #appInfoFuture(AsyncHttpClientHelper)}, and {@code appEndpoints(...)} methods are required for the application to
 * function at a basic level. This class will install {@link AppMetricsGuiceModule} to initialize metrics gathering
 * and reporting, it will install {@link AppSecurityGuiceModule} to initialize app security and auth functionality,
 * and it will install {@link AppEurekaGuiceModule} to initialize Eureka registration if you're using Eureka.
 *
 * <p>You can put anything else in this class that you want to be available for injection in your {@link Endpoint}s. As
 * you add endpoints just add them to the {@code appEndpoints(...)} argument list and return them from that method.
 * Injection will work for those endpoint classes and they will be auto-registered with the application and ready to
 * accept requests.
 *
 * <p>NOTE: See the comments and example cleanup instructions in {@link AppSecurityGuiceModule} and
 * {@link AppEurekaGuiceModule} when you're done with the examples. For example, you may want to follow the inline
 * comment instructions in {@link AppSecurityGuiceModule#authProtectedEndpoints(Set)} to enable protection of all
 * non-healthcheck endpoints. Alternately you can choose only specific endpoints to protect, or disable auth entirely,
 * or switch to a different auth scheme, or remove security validation entirely depending on your application's
 * requirements.
 *
 * @author Nic Munroe
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class AppGuiceModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Config appConfig;

    public AppGuiceModule(Config appConfig) {
        if (appConfig == null) {
            throw new IllegalArgumentException("appConfig cannot be null");
        }

        this.appConfig = appConfig;
    }

    @Override
    protected void configure() {
        install(new AppMetricsGuiceModule());
        install(new AppSecurityGuiceModule());
        install(new AppEurekaGuiceModule(appConfig));
    }

    @Provides
    @Singleton
    @Named("appEndpoints")
    public Set<Endpoint<?>> appEndpoints(
        HealthCheckEndpoint healthCheckEndpoint,
        // TODO: EXAMPLE CLEANUP - Remove these example endpoints from this method's arguments and don't return them from this method.
        ExampleEndpoint.Get exampleEndpointGet,
        ExampleEndpoint.Post exampleEndpointPost,
        ExampleDownstreamHttpAsyncEndpoint exampleDownstreamHttpAsyncEndpoint,
        ExampleProxyRouterEndpoint exampleProxyRouterEndpoint,
        ExampleBasicAuthProtectedEndpoint.Get exampleBasicAuthProtectedEndpointGet,
        ExampleBasicAuthProtectedEndpoint.Post exampleBasicAuthProtectedEndpointPost
    ) {
        return new LinkedHashSet<>(Arrays.<Endpoint<?>>asList(
            healthCheckEndpoint,
            // Example endpoints
            exampleEndpointGet, exampleEndpointPost,
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
    @Named("appInfoFuture")
    public CompletableFuture<AppInfo> appInfoFuture(AsyncHttpClientHelper asyncHttpClientHelper) {
        return AwsUtil.getAppInfoFutureWithAwsInfo(asyncHttpClientHelper);
    }
}
