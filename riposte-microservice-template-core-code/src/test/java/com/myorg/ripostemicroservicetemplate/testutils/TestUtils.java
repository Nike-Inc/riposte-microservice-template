package com.myorg.ripostemicroservicetemplate.testutils;

import com.nike.backstopper.apierror.ApiError;
import com.nike.backstopper.model.DefaultErrorContractDTO;
import com.nike.backstopper.model.DefaultErrorDTO;
import com.nike.internal.util.Pair;
import com.nike.riposte.server.Server;
import com.nike.riposte.server.config.ServerConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myorg.ripostemicroservicetemplate.server.config.AppServerConfig;
import com.typesafe.config.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import io.restassured.response.ExtractableResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains static helper methods for performing some common test tasks, mainly around launching a real server to test
 * against ({@link #createServerForTesting()}).
 *
 * @author Nic Munroe
 */
public class TestUtils {

    public static final String APP_ID = "riposte-microservice-template";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Creates an application server for testing purposes that finds a random free port to attach to, uses the
     * "compiletimetest" environment properties file, but is otherwise a fully functional and running server. You can
     * query the returned server config's {@link ServerConfig#endpointsPort()} to discover the port the server attached
     * to and {@link AppServerConfigForTesting#getTestingAppConfig()} to get a handle on the "compiletimetest" config
     * that was loaded.
     */
    public static Pair<Server, AppServerConfigForTesting> createServerForTesting() throws IOException {
        TypesafeConfigPropertiesRegistrationGuiceModuleForTesting propsRegistrationModule =
            new TypesafeConfigPropertiesRegistrationGuiceModuleForTesting(APP_ID, "compiletimetest");

        AppServerConfigForTesting serverConfig = new TestUtils.AppServerConfigForTesting(propsRegistrationModule);
        Server server = new Server(serverConfig);
        return Pair.of(server, serverConfig);
    }

    /**
     * Finds an unused port on the machine hosting the currently running JVM.
     */
    public static int findFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }

    /**
     * Helper method for component tests that verifies that the given {@code response} contains an error contract
     * matching the given {@code expectedError}.
     *
     * @param response The response to check.
     * @param expectedError The error that the response should match.
     */
    public static void verifyExpectedError(ExtractableResponse response, ApiError expectedError) {
        verifyExpectedErrors(response, expectedError.getHttpStatusCode(), Collections.singleton(expectedError));
    }

    /**
     * Helper method for component tests that verifies that the given {@code response} contains an error contract
     * matching the given collection of {@code expectedErrors} and that the HTTP status code received is the given
     * {@code expectedHttpStatusCode}.
     *
     * @param response The response to check.
     * @param expectedHttpStatusCode The HTTP status code that the response should match.
     * @param expectedErrors The errors that the response should match.
     */
    public static void verifyExpectedErrors(ExtractableResponse response, int expectedHttpStatusCode,
                                            Collection<ApiError> expectedErrors) {
        try {
            assertThat(response.statusCode()).isEqualTo(expectedHttpStatusCode);
            DefaultErrorContractDTO errorContract =
                OBJECT_MAPPER.readValue(response.asString(), DefaultErrorContractDTO.class);
            assertThat(errorContract.errors).hasSameSizeAs(expectedErrors);
            for (ApiError expectedError : expectedErrors) {
                Optional<DefaultErrorDTO> matchingError = errorContract.errors.stream().filter(
                    error -> (error.code.equals(expectedError.getErrorCode())
                              && error.message .equals(expectedError.getMessage()))
                ).findAny();

                assertThat(matchingError)
                    .overridingErrorMessage("Unable to find an error in the response contract that matches: "
                                            + expectedError.getName() + ". Actual response payload: "
                                            + response.asString())
                    .isPresent();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class AppServerConfigForTesting extends AppServerConfig {

        private final Config testingAppConfig;
        private final int portToUse;

        @SuppressWarnings("WeakerAccess")
        public AppServerConfigForTesting(
            TypesafeConfigPropertiesRegistrationGuiceModuleForTesting propertiesRegistrationModule
        ) throws IOException {

            super(propertiesRegistrationModule.getConfig(), propertiesRegistrationModule);
            this.portToUse = findFreePort();
            this.testingAppConfig = propertiesRegistrationModule.getConfig();
        }

        public Config getTestingAppConfig() {
            return testingAppConfig;
        }

        @Override
        public int endpointsPort() {
            return portToUse;
        }

    }
}
