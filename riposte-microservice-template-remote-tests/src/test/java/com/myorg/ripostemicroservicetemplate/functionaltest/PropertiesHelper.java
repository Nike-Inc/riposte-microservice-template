package com.myorg.ripostemicroservicetemplate.functionaltest;

import com.nike.backstopper.apierror.ApiError;
import com.nike.guice.typesafeconfig.TypesafeConfigPropertiesRegistrationGuiceModule;
import com.nike.riposte.typesafeconfig.util.TypesafeConfigUtil;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.myorg.ripostemicroservicetemplate.testutils.TestUtils;
import com.typesafe.config.Config;

import java.util.Base64;
import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import io.netty.util.CharsetUtil;
import io.restassured.response.ExtractableResponse;

/**
 * Helper that automates the extraction of functional test properties from the appropriate properties files and provides
 * a few helper methods like {@link #verifyExpectedError(ExtractableResponse, ApiError)}.
 *
 * <p>Other classes can simply reference {@link #INSTANCE}, and from there get access to any of the public fields or
 * methods in this class.
 *
 * @author Nic Munroe
 */
@SuppressWarnings("unused")
public class PropertiesHelper {

    @SuppressWarnings("WeakerAccess")
    public static final PropertiesHelper INSTANCE = generateInstance();

    public final String ripostemicroservicetemplateHost;
    public final String basicAuthHeaderVal;

    @Inject
    private PropertiesHelper(
        @Named("ripostemicroservicetemplate.host") String ripostemicroservicetemplateHost,
        @Named("basicAuth.username") String basicAuthUsername,
        @Named("basicAuth.password") String basicAuthPassword
    ) {
        this.ripostemicroservicetemplateHost = ripostemicroservicetemplateHost;
        this.basicAuthHeaderVal = "Basic " + Base64.getEncoder().encodeToString(
            (basicAuthUsername + ":" + basicAuthPassword).getBytes(CharsetUtil.UTF_8)
        );
    }

    public static PropertiesHelper getInstance() {
        return INSTANCE;
    }

    private static PropertiesHelper generateInstance() {
        String appId = "riposte-microservice-template-functionaltest";
        String environment = getEnvironment();
        if (environment == null) {
            throw new IllegalStateException(
                "ERROR: You must specify the remoteTestEnv System property when running functional tests. Valid options"
                + " are: local, test, or prod. e.g. -DremoteTestEnv=test"
            );
        }

        Config functionalTestConfig = TypesafeConfigUtil.loadConfigForAppIdAndEnvironment(appId, environment);
        Injector injector = Guice.createInjector(
            new TypesafeConfigPropertiesRegistrationGuiceModule(functionalTestConfig)
        );
        return injector.getInstance(PropertiesHelper.class);
    }

    private static String getEnvironment() {
        return System.getProperty("remoteTestEnv");
    }

    /**
     * Helper method for functional tests that verifies that the given {@code response} contains an error contract
     * matching the given {@code expectedError}.
     *
     * @param response The response to check.
     * @param expectedError The error that the response should match.
     */
    public void verifyExpectedError(ExtractableResponse response, ApiError expectedError) {
        // No need to copy/paste - just delegate to TestUtils
        TestUtils.verifyExpectedError(response, expectedError);
    }

    /**
     * Helper method for functional tests that verifies that the given {@code response} contains an error contract
     * matching the given collection of {@code expectedErrors} and that the HTTP status code received is the given
     * {@code expectedHttpStatusCode}.
     *
     * @param response The response to check.
     * @param expectedHttpStatusCode The HTTP status code that the response should match.
     * @param expectedErrors The errors that the response should match.
     */
    public void verifyExpectedErrors(ExtractableResponse response, int expectedHttpStatusCode,
                                     Collection<ApiError> expectedErrors) {
        // No need to copy/paste - just delegate to TestUtils
        TestUtils.verifyExpectedErrors(response, expectedHttpStatusCode, expectedErrors);
    }

}
