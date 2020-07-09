package com.myorg.ripostemicroservicetemplate.server.config.guice;

import com.nike.riposte.server.config.ServerConfig;
import com.nike.riposte.server.hooks.PostServerStartupHook;
import com.nike.riposte.server.hooks.ServerShutdownHook;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;

import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;

/**
 * A Guice module for encapsulating Eureka stuff.
 */
@SuppressWarnings("unused")
public class AppEurekaGuiceModule extends AbstractModule {

    @SuppressWarnings("FieldCanBeLocal")
    private final Config appConfig;

    public AppEurekaGuiceModule(Config appConfig) {
        this.appConfig = appConfig;
    }

    @Provides
    @Singleton
    public EurekaServerHooks eurekaServerHooks() {
        return new EurekaServerHooks(null, null);

        // TODO: EXAMPLE CLEANUP - If you need Eureka, then uncomment the code below and delete the return statement
        //      above (after enabling the riposte-service-registration-eureka dependency in build.gradle).
        //      If you don't need Eureka then you can leave this as-is, or you can delete this module entirely.
//        com.nike.riposte.serviceregistration.eureka.EurekaServerHook hook =
//            new com.nike.riposte.serviceregistration.eureka.EurekaServerHook(
//                () -> appConfig.getBoolean(
//                    com.nike.riposte.serviceregistration.eureka.EurekaHandler.DISABLE_EUREKA_INTEGRATION
//                ),
//                () -> appConfig.getString(
//                    com.nike.riposte.serviceregistration.eureka.EurekaHandler.EUREKA_DATACENTER_TYPE_PROP_NAME
//                )
//            );
//        return new EurekaServerHooks(hook, hook);
    }

    /**
     * Class holding the Riposte server startup and shutdown hooks that will startup and shutdown Eureka registration.
     * These hooks should be returned by the app's {@link ServerConfig#postServerStartupHooks()} and
     * {@link ServerConfig#serverShutdownHooks()} methods in order to work.
     */
    public static class EurekaServerHooks {

        /**
         * The startup hook that should be used to register this app in Eureka when the Riposte server starts up,
         * or null if Eureka integration is disabled.
         */
        public final @Nullable PostServerStartupHook eurekaStartupHook;
        /**
         * The shutdown hook that should be used to deregister this app in Eureka when the Riposte server shuts down,
         * or null if Eureka integration is disabled.
         */
        public final @Nullable ServerShutdownHook eurekaShutdownHook;

        public EurekaServerHooks(
            @Nullable PostServerStartupHook eurekaStartupHook,
            @Nullable ServerShutdownHook eurekaShutdownHook
        ) {
            this.eurekaStartupHook = eurekaStartupHook;
            this.eurekaShutdownHook = eurekaShutdownHook;
        }
    }
}
