package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.riposte.metrics.codahale.CodahaleMetricsListener;
import com.nike.riposte.server.config.AppInfo;
import com.nike.riposte.server.config.impl.DependencyInjectionProvidedServerConfigValuesBase;
import com.nike.riposte.server.error.handler.RiposteErrorHandler;
import com.nike.riposte.server.error.handler.RiposteUnhandledErrorHandler;
import com.nike.riposte.server.error.validation.RequestSecurityValidator;
import com.nike.riposte.server.error.validation.RequestValidator;
import com.nike.riposte.server.http.Endpoint;

import com.myorg.ripostemicroservicetemplate.server.config.AppServerConfig;
import com.myorg.ripostemicroservicetemplate.server.config.guice.AppEurekaGuiceModule.EurekaServerHooks;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * An extension of {@link DependencyInjectionProvidedServerConfigValuesBase} that includes all the extra
 * dependency-injected properties needed by {@link AppServerConfig}.
 */
public class GuiceProvidedServerConfigValues extends DependencyInjectionProvidedServerConfigValuesBase {

    public final RiposteErrorHandler riposteErrorHandler;
    public final RiposteUnhandledErrorHandler riposteUnhandledErrorHandler;
    public final RequestValidator validationService;
    public final CompletableFuture<AppInfo> appInfoFuture;
    public final @Nullable CodahaleMetricsListener metricsListener;
    // TODO: EXAMPLE CLEANUP - Do you use Eureka and/or basic auth? If not then you can delete references to them here,
    //       remove the creation of them in `AppGuiceModule`, and fix `AppServerConfig` to not attempt to use them.
    public final EurekaServerHooks eurekaServerHooks;
    public final @Nullable RequestSecurityValidator authSecurityValidator;

    @Inject
    public GuiceProvidedServerConfigValues(
        @Named("endpoints.port") Integer endpointsPort,
        @Named("endpoints.sslPort") Integer endpointsSslPort,
        @Named("endpoints.useSsl") Boolean endpointsUseSsl,
        @Named("netty.bossThreadCount") Integer numBossThreads,
        @Named("netty.workerThreadCount") Integer numWorkerThreads,
        @Named("netty.maxRequestSizeInBytes") Integer maxRequestSizeInBytes,
        @Named("appEndpoints") Set<Endpoint<?>> appEndpoints,
        @Named("debugActionsEnabled") Boolean debugActionsEnabled,
        @Named("debugChannelLifecycleLoggingEnabled") Boolean debugChannelLifecycleLoggingEnabled,
        RiposteErrorHandler riposteErrorHandler,
        RiposteUnhandledErrorHandler riposteUnhandledErrorHandler,
        RequestValidator validationService,
        @Named("appInfoFuture") CompletableFuture<AppInfo> appInfoFuture,
        @Nullable CodahaleMetricsListener metricsListener,
        EurekaServerHooks eurekaServerHooks,
        @Nullable RequestSecurityValidator authSecurityValidator
    ) {
        super(
            endpointsPort, endpointsSslPort, endpointsUseSsl, numBossThreads, numWorkerThreads, maxRequestSizeInBytes,
            appEndpoints, debugActionsEnabled, debugChannelLifecycleLoggingEnabled
        );

        this.riposteErrorHandler = riposteErrorHandler;
        this.riposteUnhandledErrorHandler = riposteUnhandledErrorHandler;
        this.validationService = validationService;
        this.eurekaServerHooks = eurekaServerHooks;
        this.metricsListener = metricsListener;
        this.appInfoFuture = appInfoFuture;
        this.authSecurityValidator = authSecurityValidator;
    }
}
