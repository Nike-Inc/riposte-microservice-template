package com.myorg

import com.myorg.ripostemicroservicetemplate.server.config.AppServerConfig
import com.nike.riposte.server.Server
import com.nike.riposte.server.config.ServerConfig
import com.nike.riposte.typesafeconfig.TypesafeConfigServer
import com.typesafe.config.Config

/**
 * Main class entry point for this app. Sets up Typesafe Config and initializes a new Riposte [Server] with the
 * application's [ServerConfig].
 *
 * @author Nic Munroe
 */
class Main : TypesafeConfigServer() {

    public override fun getServerConfig(appConfig: Config): ServerConfig {
        return AppServerConfig(appConfig)
    }
}

fun main(args: Array<String>) {
    Main().launchServer(args)
}
