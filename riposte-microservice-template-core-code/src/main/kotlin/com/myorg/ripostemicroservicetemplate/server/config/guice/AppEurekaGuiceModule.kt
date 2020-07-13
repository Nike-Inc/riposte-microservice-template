package com.myorg.ripostemicroservicetemplate.server.config.guice

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.nike.riposte.server.config.ServerConfig
import com.nike.riposte.server.hooks.PostServerStartupHook
import com.nike.riposte.server.hooks.ServerShutdownHook
import com.typesafe.config.Config
import javax.inject.Singleton

/**
 * A Guice module for encapsulating Eureka stuff.
 */
@Suppress("unused")
class AppEurekaGuiceModule(private val appConfig: Config) : AbstractModule() {

    @Provides
    @Singleton
    fun eurekaServerHooks(): EurekaServerHooks {
        return EurekaServerHooks(null, null)

        // TODO: EXAMPLE CLEANUP - If you need Eureka, then uncomment the code below and delete the return statement
        //      above (after enabling the riposte-service-registration-eureka dependency in build.gradle).
        //      If you don't need Eureka then you can leave this as-is, or you can delete this module entirely.

//        val hook: com.nike.riposte.serviceregistration.eureka.EurekaServerHook =
//            com.nike.riposte.serviceregistration.eureka.EurekaServerHook(
//                java.util.function.Supplier {
//                    appConfig.getBoolean(
//                        com.nike.riposte.serviceregistration.eureka.EurekaHandler.DISABLE_EUREKA_INTEGRATION
//                    )
//                },
//                java.util.function.Supplier {
//                   appConfig.getString(
//                       com.nike.riposte.serviceregistration.eureka.EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME
//                   )
//               }
//            )
//        return EurekaServerHooks(hook, hook)
    }

    /**
     * Class holding the Riposte server startup and shutdown hooks that will startup and shutdown Eureka registration.
     * These hooks should be returned by the app's [ServerConfig.postServerStartupHooks] and
     * [ServerConfig.serverShutdownHooks] methods in order to work.
     */
    class EurekaServerHooks(
        /**
         * The startup hook that should be used to register this app in Eureka when the Riposte server starts up,
         * or null if Eureka integration is disabled.
         */
        val eurekaStartupHook: PostServerStartupHook?,
        /**
         * The shutdown hook that should be used to deregister this app in Eureka when the Riposte server shuts down,
         * or null if Eureka integration is disabled.
         */
        val eurekaShutdownHook: ServerShutdownHook?
    )
}
