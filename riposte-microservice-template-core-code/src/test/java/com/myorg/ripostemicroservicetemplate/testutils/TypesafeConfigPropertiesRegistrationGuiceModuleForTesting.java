package com.myorg.ripostemicroservicetemplate.testutils;

import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule;
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil;

import com.typesafe.config.Config;

/**
 * An extension of {@link TypesafeConfigPropertiesRegistrationGuiceModule} that forces Typesafe Config to be loaded with
 * the given app ID and environment. Useful for loading the compiletimetest environment properties file when unit
 * testing so you can keep your unit test props separated from your local/test/prod properties.
 *
 * @author Nic Munroe
 */
public class TypesafeConfigPropertiesRegistrationGuiceModuleForTesting
    extends TypesafeConfigPropertiesRegistrationGuiceModule {

    private final String appIdToUse;
    private final String environmentToUse;
    private final Config config;

    private static Config tempStaticConfig;

    @SuppressWarnings("WeakerAccess")
    public TypesafeConfigPropertiesRegistrationGuiceModuleForTesting(String appIdToUse, String environmentToUse) {
        super(generateConfig(appIdToUse, environmentToUse));
        this.appIdToUse = appIdToUse;
        this.environmentToUse = environmentToUse;
        this.config = tempStaticConfig;
    }

    private static Config generateConfig(String appIdToUse, String environmentToUse) {
        tempStaticConfig = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(appIdToUse, environmentToUse);
        return tempStaticConfig;
    }

    public Config getConfig() {
        return config;
    }

    @Override
    protected void configure() {
        // Set the appropriate System properties to match the appIdToUse and environmentToUse.
        System.setProperty("@appId", appIdToUse);
        System.setProperty("@environment", environmentToUse);

        super.configure();
    }

}
