package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator;
import com.nike.riposte.server.error.validation.RequestSecurityValidator;
import com.nike.riposte.server.http.Endpoint;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * A Guice module for encapsulating the configuration of the {@link RequestSecurityValidator} that the app will use.
 */
public class AppSecurityGuiceModule extends AbstractModule {

    @Provides
    @Singleton
    @Named("authProtectedEndpoints")
    public List<Endpoint<?>> authProtectedEndpoints(@Named("appEndpoints") Set<Endpoint<?>> endpoints) {
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
    @SuppressWarnings("unused")
    public RequestSecurityValidator authSecurityValidator(
        @Named("authProtectedEndpoints") List<Endpoint<?>> authProtectedEndpoints,
        @Named("exampleBasicAuth.username") String basicAuthUsername,
        @Named("exampleBasicAuth.password") String basicAuthPassword
    ) {
        // TODO: EXAMPLE CLEANUP - If you want to use a different auth scheme than basic auth then you can return a
        //      different implementation instead of BasicAuthSecurityValidator. And if you want to disable auth
        //      entirely for your app, you can simply return null here.
        return new BasicAuthSecurityValidator(authProtectedEndpoints, basicAuthUsername, basicAuthPassword);
    }
}
