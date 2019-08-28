package com.myorg.ripostemicroservicetemplate.server.config.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleBasicAuthProtectedEndpoint
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleCassandraAsyncEndpoint
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleDownstreamHttpAsyncEndpoint
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint
import com.myorg.ripostemicroservicetemplate.endpoints.ExampleProxyRouterEndpoint
import com.myorg.ripostemicroservicetemplate.endpoints.HealthCheckEndpoint
import com.myorg.ripostemicroservicetemplate.error.ProjectApiErrorsImpl
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors
import com.nike.riposte.client.asynchttp.ning.AsyncHttpClientHelper
import com.nike.riposte.metrics.codahale.CodahaleMetricsCollector
import com.nike.riposte.metrics.codahale.CodahaleMetricsEngine
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener
import com.nike.riposte.metrics.codahale.ReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultGraphiteReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultJMXReporterFactory
import com.nike.riposte.metrics.codahale.contrib.DefaultSLF4jReporterFactory
import com.nike.riposte.server.config.AppInfo
import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator
import com.nike.riposte.server.http.Endpoint
import com.nike.riposte.serviceregistration.eureka.EurekaHandler
import com.nike.riposte.serviceregistration.eureka.EurekaServerHook
import com.nike.riposte.util.AwsUtil
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.LinkedHashSet
import java.util.concurrent.CompletableFuture
import javax.annotation.Nullable
import javax.inject.Named
import javax.inject.Singleton
import javax.validation.Validation
import javax.validation.Validator

/**
 * The main Guice module for the application. The [validator], [projectApiErrors], [appInfoFuture], and
 * [appEndpoints] methods are required for the application to function at a basic level. [eurekaServerHook], metrics
 * related methods, and [basicAuthSecurityValidator] are used to enable Eureka registration, metrics gathering and
 * reporting, and basic auth endpoint protection features respectively.
 *
 * You can put anything else in this class that you want to be available for injection in your [Endpoint]s. As
 * you add endpoints just add them to the [appEndpoints] argument list and return them from that method.
 * Injection will work for those endpoint classes and they will be auto-registered with the application and ready to
 * accept requests.
 *
 * NOTE: See the implementation of [basicAuthProtectedEndpoints] when you're done with the example
 * endpoints and are ready to create your application's real endpoints. You may want to follow the inline comment
 * instructions in that method to enable protection of all non-healthcheck endpoints. Alternately you can choose only
 * specific endpoints to protect, or disable basic auth entirely, or switch to a different auth scheme, or remove
 * security validation entirely depending on your application's requirements.
 *
 * @author Nic Munroe
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class AppGuiceModule(appConfig: Config?) : AbstractModule() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private val appConfig: Config = appConfig ?: throw IllegalArgumentException("appConfig cannot be null")

    override fun configure() {}

    @Provides
    @Singleton
    @Named("appEndpoints")
    fun appEndpoints(
            healthCheckEndpoint: HealthCheckEndpoint,
            // TODO: EXAMPLE CLEANUP - Remove these example endpoints from this method's arguments and don't return them from this method.
            exampleEndpointGet: ExampleEndpoint.Get,
            exampleEndpointPost: ExampleEndpoint.Post,
            exampleCassandraAsyncEndpoint: ExampleCassandraAsyncEndpoint,
            exampleDownstreamHttpAsyncEndpoint: ExampleDownstreamHttpAsyncEndpoint,
            exampleProxyRouterEndpoint: ExampleProxyRouterEndpoint,
            exampleBasicAuthProtectedEndpointGet: ExampleBasicAuthProtectedEndpoint.Get,
            exampleBasicAuthProtectedEndpointPost: ExampleBasicAuthProtectedEndpoint.Post
    ): Set<Endpoint<*>> {
        return LinkedHashSet(
            listOf(
                healthCheckEndpoint,
                // Example endpoints
                exampleEndpointGet, exampleEndpointPost, exampleCassandraAsyncEndpoint,
                exampleDownstreamHttpAsyncEndpoint, exampleProxyRouterEndpoint,
                exampleBasicAuthProtectedEndpointGet, exampleBasicAuthProtectedEndpointPost
            )
        )
    }

    @Provides
    @Singleton
    fun validator(): Validator {
        return Validation.buildDefaultValidatorFactory().validator
    }

    @Provides
    @Singleton
    fun projectApiErrors(): ProjectApiErrors {
        return ProjectApiErrorsImpl()
    }

    @Provides
    @Singleton
    fun asyncHttpClientHelper(): AsyncHttpClientHelper {
        return AsyncHttpClientHelper()
    }

    @Provides
    @Singleton
    fun eurekaServerHook(): EurekaServerHook {
        return EurekaServerHook(
                { appConfig.getBoolean(EurekaHandler.DISABLE_EUREKA_INTEGRATION) },
                { appConfig.getString(EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME) }
        )
    }

    @Provides
    @Singleton
    fun metricsListener(
            @Nullable metricsCollector: CodahaleMetricsCollector?,
            // We don't actually need the CodahaleMetricsEngine, but we ask for it here to guarantee that it is created
            //      and started.
            @Suppress("UNUSED_PARAMETER")
            @Nullable engine: CodahaleMetricsEngine?
    ): CodahaleMetricsListener? {
        return if (metricsCollector == null) null else CodahaleMetricsListener(metricsCollector)
    }

    @Provides
    @Singleton
    @JvmSuppressWildcards
    fun codahaleMetricsEngine(
            @Nullable cmc: CodahaleMetricsCollector?,
            @Nullable reporters: List<ReporterFactory>?,
            @Named("metrics.reportJvmMetrics") reportJvmMetrics: Boolean
    ): CodahaleMetricsEngine? {
        if (cmc == null)
            return null

        val engine = CodahaleMetricsEngine(cmc, reporters ?: emptyList())
        if (reportJvmMetrics) {
            engine.reportJvmMetrics()
        }
        engine.start()
        return engine
    }

    @Provides
    @Singleton
    @JvmSuppressWildcards
    fun codahaleMetricsCollector(@Nullable reporters: List<ReporterFactory>?): CodahaleMetricsCollector? {
        return if (reporters == null) null else CodahaleMetricsCollector()
    }

    @Provides
    @Singleton
    fun metricsReporters(
            @Named("metrics.slf4j.reporting.enabled") slf4jReportingEnabled: Boolean,
            @Named("metrics.jmx.reporting.enabled") jmxReportingEnabled: Boolean,
            @Named("metrics.graphite.url") graphiteUrl: String,
            @Named("metrics.graphite.port") graphitePort: Int,
            @Named("metrics.graphite.reporting.enabled") graphiteEnabled: Boolean,
            @Named("appInfoFuture") appInfoFuture: CompletableFuture<AppInfo>
    ): List<ReporterFactory>? {
        val reporters = ArrayList<ReporterFactory>()

        if (slf4jReportingEnabled)
            reporters.add(DefaultSLF4jReporterFactory())

        if (jmxReportingEnabled)
            reporters.add(DefaultJMXReporterFactory())

        if (graphiteEnabled) {
            val appInfo = appInfoFuture.join()
            val graphitePrefix = (appInfo.appId() + "." + appInfo.dataCenter() + "." + appInfo.environment()
                    + "." + appInfo.instanceId())
            reporters.add(DefaultGraphiteReporterFactory(graphitePrefix, graphiteUrl, graphitePort))
        }

        if (reporters.isEmpty()) {
            logger.info("No metrics reporters enabled - disabling metrics entirely.")
            return null
        }

        val metricReporterTypes = reporters.joinToString(",", "[", "]") { rf -> rf.javaClass.simpleName }
        logger.info("Metrics reporters enabled. metric_reporter_types={}", metricReporterTypes)

        return reporters
    }

    @Provides
    @Singleton
    @Named("basicAuthProtectedEndpoints")
    @JvmSuppressWildcards
    fun basicAuthProtectedEndpoints(@Named("appEndpoints") endpoints: Set<Endpoint<*>>): List<Endpoint<*>> {
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
    fun basicAuthSecurityValidator(
            @Named("basicAuthProtectedEndpoints") basicAuthProtectedEndpoints: List<Endpoint<*>>,
            @Named("exampleBasicAuth.username") basicAuthUsername: String,
            @Named("exampleBasicAuth.password") basicAuthPassword: String
    ): BasicAuthSecurityValidator {
        return BasicAuthSecurityValidator(basicAuthProtectedEndpoints, basicAuthUsername, basicAuthPassword)
    }

    @Provides
    @Singleton
    @Named("appInfoFuture")
    fun appInfoFuture(asyncHttpClientHelper: AsyncHttpClientHelper): CompletableFuture<AppInfo> {
        return AwsUtil.getAppInfoFutureWithAwsInfo(asyncHttpClientHelper)
    }
}
