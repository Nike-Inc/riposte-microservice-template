package com.myorg.ripostemicroservicetemplate.testutils

import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil
import com.typesafe.config.Config

/**
 * An extension of [TypesafeConfigPropertiesRegistrationGuiceModule] that forces Typesafe Config to be loaded with
 * the given app ID and environment. Useful for loading the compiletimetest environment properties file when unit
 * testing so you can keep your unit test props separated from your local/test/prod properties.
 */
class TypesafeConfigPropertiesRegistrationGuiceModuleForTesting(
    private val appIdToUse: String,
    private val environmentToUse: String
) : TypesafeConfigPropertiesRegistrationGuiceModule(generateConfig(appIdToUse, environmentToUse)) {
    val config: Config = tempStaticConfig!!

    override fun configure() {
        // Set the appropriate System properties to match the appIdToUse and environmentToUse.
        System.setProperty("@appId", appIdToUse)
        System.setProperty("@environment", environmentToUse)

        super.configure()
    }

    companion object {

        private var tempStaticConfig: Config? = null

        private fun generateConfig(appIdToUse: String, environmentToUse: String): Config {
            tempStaticConfig = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(appIdToUse, environmentToUse)
            return tempStaticConfig!!
        }
    }
}
