package com.myorg.ripostemicroservicetemplate.server.config.guice

import com.myorg.ripostemicroservicetemplate.server.config.AppServerConfig
import com.nike.riposte.metrics.codahale.CodahaleMetricsListener
import com.nike.riposte.server.config.AppInfo
import com.nike.riposte.server.config.impl.DependencyInjectionProvidedServerConfigValuesBase
import com.nike.riposte.server.error.handler.RiposteErrorHandler
import com.nike.riposte.server.error.handler.RiposteUnhandledErrorHandler
import com.nike.riposte.server.error.validation.BasicAuthSecurityValidator
import com.nike.riposte.server.error.validation.RequestValidator
import com.nike.riposte.server.http.Endpoint
import com.nike.riposte.serviceregistration.eureka.EurekaServerHook
import java.util.concurrent.CompletableFuture
import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named

/**
 * An extension of [DependencyInjectionProvidedServerConfigValuesBase] that includes all the extra
 * dependency-injected properties needed by [AppServerConfig].
 *
 * @author Nic Munroe
 */
class GuiceProvidedServerConfigValues
@Inject
constructor(
    @Named("endpoints.port") endpointsPort: Int?,
    @Named("endpoints.sslPort") endpointsSslPort: Int?,
    @Named("endpoints.useSsl") endpointsUseSsl: Boolean?,
    @Named("netty.bossThreadCount") numBossThreads: Int?,
    @Named("netty.workerThreadCount") numWorkerThreads: Int?,
    @Named("netty.maxRequestSizeInBytes") maxRequestSizeInBytes: Int?,
    @Named("appEndpoints") appEndpoints: Set<@JvmSuppressWildcards Endpoint<*>>,
    @Named("debugActionsEnabled") debugActionsEnabled: Boolean?,
    @Named("debugChannelLifecycleLoggingEnabled") debugChannelLifecycleLoggingEnabled: Boolean?,
    val riposteErrorHandler: RiposteErrorHandler,
    val riposteUnhandledErrorHandler: RiposteUnhandledErrorHandler,
    val validationService: RequestValidator,
    @Named("appInfoFuture") val appInfoFuture: CompletableFuture<AppInfo>,
    @Nullable val metricsListener: CodahaleMetricsListener?,
    // TODO: EXAMPLE CLEANUP - Do you use Eureka and/or basic auth? If not then you can delete references to them here,
    //       remove the creation of them in `AppGuiceModule`, and fix `AppServerConfig` to not attempt to use them.
    val eurekaServerHook: EurekaServerHook,
    val basicAuthSecurityValidator: BasicAuthSecurityValidator
) : DependencyInjectionProvidedServerConfigValuesBase(
        endpointsPort, endpointsSslPort, endpointsUseSsl, numBossThreads, numWorkerThreads, maxRequestSizeInBytes,
        appEndpoints, debugActionsEnabled, debugChannelLifecycleLoggingEnabled
)
