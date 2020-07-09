package com.myorg.ripostemicroservicetemplate.server.config;

import com.nike.backstopper.handler.riposte.config.guice.BackstopperRiposteConfigGuiceModule;
import com.nike.guice.PropertiesRegistrationGuiceModule;
import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule;
import com.nike.riposte.metrics.MetricsListener;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.config.ServerConfig;
import com.nike.riposte.server.error.handler.RiposteErrorHandler;
import com.nike.riposte.server.error.handler.RiposteUnhandledErrorHandler;
import com.nike.riposte.server.error.validation.RequestSecurityValidator;
import com.nike.riposte.server.error.validation.RequestValidator;
import com.nike.riposte.server.hooks.PostServerStartupHook;
import com.nike.riposte.server.hooks.ServerShutdownHook;
import com.nike.riposte.server.http.Endpoint;
import com.nike.riposte.server.logging.AccessLogger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.myorg.ripostemicroservicetemplate.server.config.guice.AppGuiceModule;
import com.myorg.ripostemicroservicetemplate.server.config.guice.GuiceProvidedServerConfigValues;
import com.typesafe.config.Config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The {@link ServerConfig} for this application. Many of the server config option values (e.g. {@link
 * ServerConfig#endpointsPort()} come from your config properties files, and this also tells the server to use {@link
 * #getAppGuiceModules(Config)} for the Guice modules for this app.
 *
 * <p>If you have more modules besides {@link AppGuiceModule} you want to use in your application just add them to the
 * {@link #getAppGuiceModules(Config)} list. You should never remove the {@link BackstopperRiposteConfigGuiceModule}
 * module from that list unless you replace it with an extension of that class that performs the same function - it is
 * what configures the application's error handling system.
 *
 * @author Nic Munroe
 */
@SuppressWarnings("WeakerAccess")
public class AppServerConfig implements ServerConfig {

    /*
        We use a GuiceProvidedServerConfigValues to generate most of the values we need to return for ServerConfig's
        methods. Some values will be provided by config files (using a PropertiesRegistrationGuiceModule), others from
        AppGuiceModule, and others from BackstopperRiposteConfigGuiceModule. Having Guice instantiate them this way
        means they will be created, finalized, and ready for use by the time the ServerConfig methods are called. No
        need for synchronized methods or lazy-loading.
     */
    protected final GuiceProvidedServerConfigValues guiceValues;
    protected final Config appConfig;

    protected final AccessLogger accessLogger = new AccessLogger();

    protected AppServerConfig(Config appConfig, PropertiesRegistrationGuiceModule propertiesRegistrationGuiceModule) {
        super();

        // Store the appConfig.
        if (appConfig == null) {
            throw new IllegalArgumentException("appConfig cannot be null");
        }

        this.appConfig = appConfig;

        // Create a Guice Injector for this app.
        List<Module> appGuiceModules = new ArrayList<>();
        appGuiceModules.add(propertiesRegistrationGuiceModule);
        appGuiceModules.addAll(getAppGuiceModules(appConfig));

        Injector appInjector = Guice.createInjector(appGuiceModules);

        // Use the new Guice Injector to create a GuiceProvidedServerConfigValues, which will contain all the
        //      guice-provided config stuff for this app.
        this.guiceValues = appInjector.getProvider(GuiceProvidedServerConfigValues.class).get();

        // Now that everything else is setup, we can initialize the metrics listener.
        if (guiceValues.metricsListener != null) {
            guiceValues.metricsListener.initEndpointAndServerConfigMetrics(this);
        }
    }

    public AppServerConfig(Config appConfig) {
        this(appConfig, new TypesafeConfigPropertiesRegistrationGuiceModule(appConfig));
    }

    protected List<Module> getAppGuiceModules(Config appConfig) {
        return Arrays.asList(
            new AppGuiceModule(appConfig),
            new BackstopperRiposteConfigGuiceModule()
        );
    }

    @Override
    public @Nullable AccessLogger accessLogger() {
        return accessLogger;
    }

    @Override
    public @Nullable CompletableFuture<@Nullable AppInfo> appInfo() {
        return guiceValues.appInfoFuture;
    }

    @Override
    public @Nullable MetricsListener metricsListener() {
        return guiceValues.metricsListener;
    }

    @Override
    public @Nullable RequestSecurityValidator requestSecurityValidator() {
        return guiceValues.authSecurityValidator;
    }

    @Override
    public @Nullable List<@NotNull PostServerStartupHook> postServerStartupHooks() {
        PostServerStartupHook eurekaStartupHook = guiceValues.eurekaServerHooks.eurekaStartupHook;
        return (eurekaStartupHook == null) ? null : Collections.singletonList(eurekaStartupHook);
    }

    @Override
    public @Nullable List<@NotNull ServerShutdownHook> serverShutdownHooks() {
        ServerShutdownHook eurekaShutdownHook = guiceValues.eurekaServerHooks.eurekaShutdownHook;
        return (eurekaShutdownHook == null) ? null : Collections.singletonList(eurekaShutdownHook);
    }

    @Override
    public @NotNull Collection<@NotNull Endpoint<?>> appEndpoints() {
        return guiceValues.appEndpoints;
    }

    @Override
    public @NotNull RiposteErrorHandler riposteErrorHandler() {
        return guiceValues.riposteErrorHandler;
    }

    @Override
    public @NotNull RiposteUnhandledErrorHandler riposteUnhandledErrorHandler() {
        return guiceValues.riposteUnhandledErrorHandler;
    }

    @Override
    public @Nullable RequestValidator requestContentValidationService() {
        return guiceValues.validationService;
    }

    @Override
    public boolean isDebugActionsEnabled() {
        return guiceValues.debugActionsEnabled;
    }

    @Override
    public boolean isDebugChannelLifecycleLoggingEnabled() {
        return guiceValues.debugChannelLifecycleLoggingEnabled;
    }

    @Override
    public int endpointsPort() {
        return guiceValues.endpointsPort;
    }

    @Override
    public int endpointsSslPort() {
        return guiceValues.endpointsSslPort;
    }

    @Override
    public boolean isEndpointsUseSsl() {
        return guiceValues.endpointsUseSsl;
    }

    @Override
    public int numBossThreads() {
        return guiceValues.numBossThreads;
    }

    @Override
    public int numWorkerThreads() {
        return guiceValues.numWorkerThreads;
    }

    @Override
    public int maxRequestSizeInBytes() {
        return guiceValues.maxRequestSizeInBytes;
    }


}
