package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;
import com.nike.riposte.client.asynchttp.ning.AsyncHttpClientHelper;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator;
import com.nike.riposte.server.http.Endpoint;
import com.nike.riposte.serviceregistration.eureka.EurekaHandler;
import com.nike.riposte.serviceregistration.eureka.EurekaServerHook;
import com.nike.riposte.util.AwsUtil;

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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.validation.Validation;
import javax.validation.Validator;

/**
 * The main Guice module for the application. The {@link #validator()}, {@link #projectApiErrors()}, {@link
 * #appInfoFuture(AsyncHttpClientHelper)}, and {@code appEndpoints(...)} methods are required for the application to
 * function at a basic level. This class will install {@link AppMetricsGuiceModule} to initialize metrics gathering
 * and reporting. {@link #eurekaServerHook()} and {@link #basicAuthSecurityValidator(List, String, String)} are used
 * to enable Eureka registration and basic auth endpoint protection features respectively.
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
        if (appConfig == null) {
            throw new IllegalArgumentException("appConfig cannot be null");
        }

        this.appConfig = appConfig;
    }

    @Override
    protected void configure() {
        install(new AppMetricsGuiceModule());
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
    public EurekaServerHook eurekaServerHook() {
        return new EurekaServerHook(
            () -> appConfig.getBoolean(EurekaHandler.DISABLE_EUREKA_INTEGRATION),
            () -> appConfig.getString(EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME)
        );
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
