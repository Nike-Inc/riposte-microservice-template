package com.myorg.ripostemicroservicetemplate.functionaltest

import com.google.inject.Guice
import com.myorg.ripostemicroservicetemplate.functionaltest.PropertiesHelper.Companion.INSTANCE
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils
import com.nike.backstopper.apierror.ApiError
import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil
import io.netty.util.CharsetUtil
import io.restassured.response.ExtractableResponse
import java.util.Base64
import javax.inject.Inject
import javax.inject.Named

/**
 * Helper that automates the extraction of functional test properties from the appropriate properties files and
 * provides a few helper methods like [verifyExpectedError].
 *
 * Other classes can simply reference [INSTANCE], and from there get access to any of the public fields or
 * methods in this class.
 *
 * @author Nic Munroe
 */
@Suppress("unused")
class PropertiesHelper
@Inject
private constructor(
    @Named("ripostemicroservicetemplate.host") val ripostemicroservicetemplateHost: String,
    @Named("basicAuth.username") basicAuthUsername: String,
    @Named("basicAuth.password") basicAuthPassword: String
) {

    val basicAuthHeaderVal: String = "Basic " + Base64.getEncoder().encodeToString(
            (basicAuthUsername + ":" + basicAuthPassword).toByteArray(CharsetUtil.UTF_8)
    )

    /**
     * Helper method for functional tests that verifies that the given `response` contains an error contract
     * matching the given `expectedError`.
     *
     * @param response The response to check.
     * @param expectedError The error that the response should match.
     */
    fun verifyExpectedError(response: ExtractableResponse<*>, expectedError: ApiError) {
        // No need to copy/paste - just delegate to TestUtils
        TestUtils.verifyExpectedError(response, expectedError)
    }

    /**
     * Helper method for functional tests that verifies that the given `response` contains an error contract
     * matching the given collection of `expectedErrors` and that the HTTP status code received is the given
     * `expectedHttpStatusCode`.
     *
     * @param response The response to check.
     * @param expectedHttpStatusCode The HTTP status code that the response should match.
     * @param expectedErrors The errors that the response should match.
     */
    fun verifyExpectedErrors(
        response: ExtractableResponse<*>,
        expectedHttpStatusCode: Int,
        expectedErrors: Collection<ApiError>
    ) {
        // No need to copy/paste - just delegate to TestUtils
        TestUtils.verifyExpectedErrors(response, expectedHttpStatusCode, expectedErrors)
    }

    companion object {

        val INSTANCE: PropertiesHelper = generateInstance()

        private fun generateInstance(): PropertiesHelper {
            val appId = "riposte-microservice-template-functionaltest"
            val environment = environment ?: throw IllegalStateException(
                    "ERROR: You must specify the remoteTestEnv System property when running functional tests. " +
                            "Valid options are: local, test, or prod. e.g. -DremoteTestEnv=test"
            )

            val functionalTestConfig = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(appId, environment)
            val injector = Guice.createInjector(
                    TypesafeConfigPropertiesRegistrationGuiceModule(functionalTestConfig)
            )
            return injector.getInstance(PropertiesHelper::class.java)
        }

        private val environment: String? get() = System.getProperty("remoteTestEnv")
    }
}
