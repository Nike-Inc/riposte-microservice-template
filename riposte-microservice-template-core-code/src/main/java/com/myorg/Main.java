package com.myorg;

import com.nike.riposte.server.Server;
import com.nike.riposte.server.config.ServerConfig;
import com.nike.riposte.typesafeconfig.TypesafeConfigServer;

import com.myorg.ripostemicroservicetemplate.server.config.AppServerConfig;
import com.typesafe.config.Config;

/**
 * Main class entry point for this app. Sets up Typesafe Config and initializes a new Riposte {@link Server} with the
 * application's {@link ServerConfig}.
 *
 * @author Nic Munroe
 */
public class Main extends TypesafeConfigServer {

    @Override
    protected ServerConfig getServerConfig(Config appConfig) {
        return new AppServerConfig(appConfig);
    }

    public static void main(String[] args) throws Exception {
        new Main().launchServer(args);
    }
}