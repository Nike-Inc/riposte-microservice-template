package com.myorg.ripostemicroservicetemplate.testutils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.myorg.ripostemicroservicetemplate.server.config.AppServerConfig
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils.createServerForTesting
import com.nike.backstopper.apierror.ApiError
import com.nike.backstopper.model.DefaultErrorContractDTO
import com.nike.internal.util.Pair
import com.nike.riposte.server.Server
import com.nike.riposte.server.config.ServerConfig
import com.typesafe.config.Config
import io.restassured.response.ExtractableResponse
import java.io.IOException
import java.lang.reflect.Field
import java.net.ServerSocket
import org.assertj.core.api.Assertions.assertThat

/**
 * Contains static helper methods for performing some common test tasks, mainly around launching a real server to test
 * against ([createServerForTesting]).
 *
 * @author Nic Munroe
 */
object TestUtils {

    const val APP_ID = "riposte-microservice-template"
    private val OBJECT_MAPPER = ObjectMapper().registerKotlinModule()

    class AppServerConfigForTesting
    constructor(
        propertiesRegistrationModule: TypesafeConfigPropertiesRegistrationGuiceModuleForTesting
    ) : AppServerConfig(propertiesRegistrationModule.config, propertiesRegistrationModule) {

        val testingAppConfig: Config = propertiesRegistrationModule.config
        private val portToUse: Int = findFreePort()

        override fun endpointsPort(): Int {
            return portToUse
        }
    }

    /**
     * Creates an application server for testing purposes that finds a random free port to attach to, uses the
     * "compiletimetest" environment properties file, but is otherwise a fully functional and running server. You can
     * query the returned server config's [ServerConfig.endpointsPort] to discover the port the server attached
     * to and [AppServerConfigForTesting.testingAppConfig] to get a handle on the "compiletimetest" config
     * that was loaded.
     */
    fun createServerForTesting(): Pair<Server, AppServerConfigForTesting> {
        val propsRegistrationModule = TypesafeConfigPropertiesRegistrationGuiceModuleForTesting(APP_ID, "compiletimetest")

        val serverConfig = AppServerConfigForTesting(propsRegistrationModule)
        val server = Server(serverConfig)

        return Pair.of(server, serverConfig)
    }

    /**
     * Finds an unused port on the machine hosting the currently running JVM.
     */
    fun findFreePort(): Int {
        ServerSocket(0).use { return it.localPort }
    }

    /**
     * Helper method for component tests that verifies that the given `response` contains an error contract
     * matching the given `expectedError`.
     *
     * @param response The response to check.
     * @param expectedError The error that the response should match.
     */
    fun verifyExpectedError(response: ExtractableResponse<*>, expectedError: ApiError) {
        verifyExpectedErrors(response, expectedError.httpStatusCode, setOf(expectedError))
    }

    /**
     * Helper method for component tests that verifies that the given `response` contains an error contract
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
        try {
            assertThat(response.statusCode()).isEqualTo(expectedHttpStatusCode)
            val errorContract = OBJECT_MAPPER.readValue<DefaultErrorContractDTO>(response.asString())
            assertThat(errorContract.errors).hasSameSizeAs(expectedErrors)
            for (expectedError in expectedErrors) {
                val matchingError = errorContract.errors.find {
                    error -> error.code == expectedError.errorCode && error.message == expectedError.message
                }

                assertThat(matchingError)
                        .overridingErrorMessage(
                                "Unable to find an error in the response contract that matches: " +
                                        "${expectedError.name}. Actual response payload: ${response.asString()}")
                        .isNotNull()
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    /**
     * A copy of the Mockito 1.x Whitebox class - needed because they dropped this class in Mockito 2.x.
     */
    object Whitebox {
        fun getInternalState(target: Any, field: String): Any {
            val c: Class<*> = target.javaClass
            try {
                val f: Field = getFieldFromHierarchy(c, field)
                f.isAccessible = true
                return f.get(target)
            } catch (e: Exception) {
                throw RuntimeException("Unable to get internal state on a private field.", e)
            }
        }

        fun setInternalState(target: Any, field: String, value: Any) {
            val c: Class<*> = target.javaClass
            try {
                val f: Field = getFieldFromHierarchy(c, field)
                f.isAccessible = true
                f.set(target, value)
            } catch (e: Exception) {
                throw RuntimeException("Unable to set internal state on a private field.", e)
            }
        }

        private fun getFieldFromHierarchy(origClass: Class<*>, field: String): Field {
            var f: Field? = getField(origClass, field)
            var clazz: Class<*> = origClass
            while (f == null && clazz != Object::class.java) {
                clazz = clazz.superclass
                f = getField(clazz, field)
            }

            if (f == null) {
                throw RuntimeException(
                    "You want me to get this field: '" + field +
                    "' on this class: '" + clazz.getSimpleName() +
                    "' but this field is not declared withing hierarchy of this class!")
            }
            return f
        }

        private fun getField(clazz: Class<*>, field: String): Field? {
            return try {
                clazz.getDeclaredField(field)
            } catch (e: NoSuchFieldException) {
                null
            }
        }
    }
}
