package com.myorg.ripostemicroservicetemplate.server.config.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint
import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator
import com.nike.riposte.server.error.validation.RequestSecurityValidator
import com.nike.riposte.server.http.Endpoint
import javax.inject.Named
import javax.inject.Singleton

/**
 * A Guice module for encapsulating the configuration of the [RequestSecurityValidator] that the app will use.
 */
@Suppress("unused")
class AppSecurityGuiceModule : AbstractModule() {

    @Provides
    @Singleton
    @Named("authProtectedEndpoints")
    @JvmSuppressWildcards
    fun authProtectedEndpoints(@Named("appEndpoints") endpoints: Set<Endpoint<*>>): List<Endpoint<*>> {
        // TODO: EXAMPLE CLEANUP - Think about what you want this method to do and adjust it accordingly.
        // For most projects you probably want everything except the healthcheck endpoint.
        //      That's the commented-out line below.
        //        return endpoints.filter { it !is HealthCheckEndpoint }

        // For the example template though, we only want the ExampleBasicAuthProtectedEndpoint.Post class protected and
        //      everything else should be allowed. Once you're done with the examples and ready to build your app you
        //      may want to delete the line below and uncomment the line above, or otherwise adjust the filter for your
        //      needs.
        return endpoints.filterIsInstance<ExampleBasicAuthProtectedEndpoint.Post>()
    }

    @Provides
    @Singleton
    @JvmSuppressWildcards
    fun authSecurityValidator(
        @Named("authProtectedEndpoints") authProtectedEndpoints: List<Endpoint<*>>,
        @Named("exampleBasicAuth.username") basicAuthUsername: String,
        @Named("exampleBasicAuth.password") basicAuthPassword: String
    ): RequestSecurityValidator {
        // TODO: EXAMPLE CLEANUP - If you want to use a different auth scheme than basic auth then you can return a
        //      different implementation instead of BasicAuthSecurityValidator. And if you want to disable auth
        //      entirely for your app, you can simply return null here.
        return BasicAuthSecurityValidator(authProtectedEndpoints, basicAuthUsername, basicAuthPassword)
    }
}
