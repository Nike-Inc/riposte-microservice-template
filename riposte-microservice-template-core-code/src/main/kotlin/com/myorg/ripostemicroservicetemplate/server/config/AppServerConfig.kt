package com.myorg.ripostemicroservicetemplate.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.Guice
import com.google.inject.Module
import com.myorg.ripostemicroservicetemplate.server.config.guice.AppGuiceModule
import com.myorg.ripostemicroservicetemplate.server.config.guice.GuiceProvidedServerConfigValues
import com.nike.backstopper.handler.riposte.config.guice.BackstopperRiposteConfigGuiceModule
import com.nike.guice.PropertiesRegistrationGuiceModule
import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule
import com.nike.riposte.metrics.MetricsListener
import com.nike.riposte.server.config.AppInfo
import com.nike.riposte.server.config.ServerConfig
import com.nike.riposte.server.error.handler.RiposteErrorHandler
import com.nike.riposte.server.error.handler.RiposteUnhandledErrorHandler
import com.nike.riposte.server.error.validation.RequestSecurityValidator
import com.nike.riposte.server.error.validation.RequestValidator
import com.nike.riposte.server.hooks.PostServerStartupHook
import com.nike.riposte.server.hooks.ServerShutdownHook
import com.nike.riposte.server.http.Endpoint
import com.nike.riposte.server.logging.AccessLogger
import com.typesafe.config.Config
import java.util.ArrayList
import java.util.Arrays
import java.util.concurrent.CompletableFuture

/**
 * The [ServerConfig] for this application. Many of the server config option values (e.g. [ServerConfig.endpointsPort])
 * come from your config properties files, and this also tells the server to use [getAppGuiceModules] for the Guice
 * modules for this app.
 *
 * If you have more modules besides [AppGuiceModule] you want to use in your application just add them to the
 * [getAppGuiceModules] list. You should never remove the [BackstopperRiposteConfigGuiceModule]
 * module from that list unless you replace it with an extension of that class that performs the same function - it is
 * what configures the application's error handling system.
 *
 * @author Nic Munroe
 */
open class AppServerConfig
protected constructor(
        appConfig: Config?,
        propertiesRegistrationGuiceModule: PropertiesRegistrationGuiceModule
) : ServerConfig {

    /*
        We use a GuiceProvidedServerConfigValues to generate most of the values we need to return for ServerConfig's
        methods. Some values will be provided by config files (using a PropertiesRegistrationGuiceModule), others from
        AppGuiceModule, and others from BackstopperRiposteConfigGuiceModule. Having Guice instantiate them this way
        means they will be created, finalized, and ready for use by the time the ServerConfig methods are called. No
        need for synchronized methods or lazy-loading.
     */
    private val guiceValues: GuiceProvidedServerConfigValues

    private val accessLogger = AccessLogger()

    private val kotlinEnabledObjectMapper: ObjectMapper = ObjectMapper().registerKotlinModule()

    private val appConfig: Config = appConfig ?: throw IllegalArgumentException("appConfig cannot be null")
    
    init {
        // Create a Guice Injector for this app.
        val appGuiceModules = ArrayList<Module>()
        appGuiceModules.add(propertiesRegistrationGuiceModule)
        @Suppress("LeakingThis")
        appGuiceModules.addAll(getAppGuiceModules(this.appConfig))

        val appInjector = Guice.createInjector(appGuiceModules)

        // Use the new Guice Injector to create a GuiceProvidedServerConfigValues, which will contain all the
        //      guice-provided config stuff for this app.
        this.guiceValues = appInjector.getProvider(GuiceProvidedServerConfigValues::class.java).get()

        // Now that everything else is setup, we can initialize the metrics listener.
        @Suppress("LeakingThis")
        if (guiceValues.metricsListener != null) {
            guiceValues.metricsListener.initEndpointAndServerConfigMetrics(this)
        }
    }

    constructor(appConfig: Config?) : this(appConfig, TypesafeConfigPropertiesRegistrationGuiceModule(appConfig))

    protected open fun getAppGuiceModules(appConfig: Config): List<Module> {
        return Arrays.asList<Module>(
                AppGuiceModule(appConfig),
                BackstopperRiposteConfigGuiceModule()
        )
    }

    override fun accessLogger(): AccessLogger {
        return accessLogger
    }

    override fun appInfo(): CompletableFuture<AppInfo> {
        return guiceValues.appInfoFuture
    }

    override fun metricsListener(): MetricsListener? {
        return guiceValues.metricsListener
    }

    override fun requestSecurityValidator(): RequestSecurityValidator {
        return guiceValues.basicAuthSecurityValidator
    }

    override fun postServerStartupHooks(): List<PostServerStartupHook> {
        return listOf<PostServerStartupHook>(guiceValues.eurekaServerHook)
    }

    override fun serverShutdownHooks(): List<ServerShutdownHook> {
        return listOf<ServerShutdownHook>(guiceValues.eurekaServerHook)
    }

    override fun appEndpoints(): Collection<Endpoint<*>> {
        return guiceValues.appEndpoints
    }

    override fun riposteErrorHandler(): RiposteErrorHandler {
        return guiceValues.riposteErrorHandler
    }

    override fun riposteUnhandledErrorHandler(): RiposteUnhandledErrorHandler {
        return guiceValues.riposteUnhandledErrorHandler
    }

    override fun requestContentValidationService(): RequestValidator {
        return guiceValues.validationService
    }

    override fun isDebugActionsEnabled(): Boolean {
        return guiceValues.debugActionsEnabled
    }

    override fun isDebugChannelLifecycleLoggingEnabled(): Boolean {
        return guiceValues.debugChannelLifecycleLoggingEnabled
    }

    override fun endpointsPort(): Int {
        return guiceValues.endpointsPort
    }

    override fun endpointsSslPort(): Int {
        return guiceValues.endpointsSslPort
    }

    override fun isEndpointsUseSsl(): Boolean {
        return guiceValues.endpointsUseSsl
    }

    override fun numBossThreads(): Int {
        return guiceValues.numBossThreads
    }

    override fun numWorkerThreads(): Int {
        return guiceValues.numWorkerThreads
    }

    override fun maxRequestSizeInBytes(): Int {
        return guiceValues.maxRequestSizeInBytes
    }

    override fun defaultRequestContentDeserializer(): ObjectMapper {
        return kotlinEnabledObjectMapper
    }

    override fun defaultResponseContentSerializer(): ObjectMapper {
        return kotlinEnabledObjectMapper
    }
}
