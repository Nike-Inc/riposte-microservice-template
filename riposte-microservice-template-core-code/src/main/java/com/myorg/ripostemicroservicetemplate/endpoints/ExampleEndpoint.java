package com.myorg.ripostemicroservicetemplate.endpoints;

import com.nike.backstopper.apierror.ApiErrorWithMetadata;
import com.nike.backstopper.exception.ApiException;
import com.nike.backstopper.handler.riposte.config.guice.BackstopperRiposteConfigGuiceModule;
import com.nike.internal.util.Pair;
import com.nike.riposte.server.http.RequestInfo;
import com.nike.riposte.server.http.ResponseInfo;
import com.nike.riposte.server.http.StandardEndpoint;
import com.nike.riposte.util.Matcher;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.myorg.ripostemicroservicetemplate.error.ProjectApiError;

import org.hibernate.validator.constraints.NotBlank;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.validation.constraints.NotNull;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;

import static java.util.Collections.singletonList;

/**
 * An example endpoint that shows how to do automatic and manual validation (in the {@link Post} inner class).
 *
 * <p>The automatic validation is done because {@link Post#isValidateRequestContent(RequestInfo)} defaults to true and
 * the {@link Post#requestContentType()} method inherited from {@link StandardEndpoint} returns a non-null value
 * (subclasses of {@link StandardEndpoint} are able to infer the input type automatically based on the generic type
 * declarations on the class). If either of those things were not true then the deserialized request content would not
 * automatically be run through the validator. The validator runs JSR 303 validation on the request's {@link
 * RequestInfo#getContent()} before calling the execute method. See the {@link ExampleEndpoint.ErrorHandlingEndpointArgs}
 * class for an example of how to annotate classes that you want to be JSR 303 validated.
 *
 * <p>The manual validation is performed in the execute method by manually throwing an {@link
 * com.nike.backstopper.exception.ApiException}.
 *
 * <p>This validation and error handling system is powered by
 * <a href="https://github.com/Nike-Inc/backstopper">Backstopper</a>. See that project's readme for more information.
 *
 * <p>TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
@SuppressWarnings("WeakerAccess")
public class ExampleEndpoint {

    public static final String MATCHING_PATH = "/example";

    /**
     * The GET implementation of /example
     */
    public static class Get extends StandardEndpoint<Void, ErrorHandlingEndpointArgs> {

        private static final Matcher MATCHER = Matcher.match(MATCHING_PATH, HttpMethod.GET);

        @Override
        public CompletableFuture<ResponseInfo<ErrorHandlingEndpointArgs>> execute(RequestInfo<Void> request,
                                                                                  Executor longRunningTaskExecutor,
                                                                                  ChannelHandlerContext ctx) {
            // Since we're not doing anything time consuming we don't need to execute anything on another thread and we
            //      can just return an already-completed CompletableFuture.
            return CompletableFuture.completedFuture(
                ResponseInfo.newBuilder(
                    new ErrorHandlingEndpointArgs(
                        "some-val1-" + UUID.randomUUID().toString(),
                        "some-val2-" + UUID.randomUUID().toString(),
                        false
                    )
                ).build()
            );
        }

        @Override
        public Matcher requestMatcher() {
            return MATCHER;
        }

    }

    /**
     * The POST implementation of /example
     */
    public static class Post extends StandardEndpoint<ErrorHandlingEndpointArgs, ErrorHandlingEndpointArgs> {

        private static final Matcher MATCHER = Matcher.match(MATCHING_PATH, HttpMethod.POST);

        /**
         * Resource endpoint that gives an example of how to use the error handling system (hooked up to Backstopper via
         * {@link BackstopperRiposteConfigGuiceModule} Guice module) to handle all the errors in your application, both
         * for object validation via JSR 303 annotations and manually thrown errors.
         *
         * @param request
         *     The incoming request. {@link RequestInfo#getContent()} contains the request body with the arguments that
         *     the client can pass (some are required, others are not - see the JSR 303 validation annotations on the
         *     {@link ErrorHandlingEndpointArgs} class to see which are which).
         */
        @Override
        public CompletableFuture<ResponseInfo<ErrorHandlingEndpointArgs>> execute(
            RequestInfo<ErrorHandlingEndpointArgs> request, Executor longRunningTaskExecutor, ChannelHandlerContext ctx
        ) {
            // If we reach here then the request content has already been run through the JSR 303 validator and we know
            //      it's non-null (since the InputType in our StandardEndpoint<InputType, OutputType> definition is
            //      not Void).
            ErrorHandlingEndpointArgs content = request.getContent();

            // Manually check the throwManualError query param (normally you'd do this with JSR 303 annotations on the
            //      object, but this shows how you can manually throw exceptions to be picked up by the error handling
            //      system).
            if (Boolean.TRUE.equals(content.throwManualError)) {
                throw ApiException.newBuilder()
                                  .withExceptionMessage("Manual error throw was requested")
                                  .withApiErrors(new ApiErrorWithMetadata(
                                      ProjectApiError.EXAMPLE_ERROR_MANUALLY_THROWN,
                                      Pair.of("dynamic_metadata", System.currentTimeMillis())
                                  ))
                                  .withExtraDetailsForLogging(Pair.of("some_important_log_info", "foo"))
                                  .withExtraResponseHeaders(
                                      Pair.of("useful-error-related-response-header", singletonList("foo"))
                                  )
                                  .build();
            }

            // Since we're not doing anything time consuming we don't need to execute anything on another thread and we
            //      can just return an already-completed CompletableFuture.
            return CompletableFuture.completedFuture(
                ResponseInfo.newBuilder(content).withHttpStatusCode(HttpResponseStatus.CREATED.code()).build()
            );
        }

        @Override
        public Matcher requestMatcher() {
            return MATCHER;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorHandlingEndpointArgs {

        @NotNull(message = "EXAMPLE_ERROR_BAD_INPUT_VAL_1")
        @NotBlank(message = "EXAMPLE_ERROR_BAD_INPUT_VAL_1")
        public final String input_val_1;

        @NotNull(message = "EXAMPLE_ERROR_BAD_INPUT_VAL_2")
        @NotBlank(message = "EXAMPLE_ERROR_BAD_INPUT_VAL_2")
        public final String input_val_2;

        public final Boolean throwManualError;

        // Here for deserialization support.
        @SuppressWarnings("unused")
        protected ErrorHandlingEndpointArgs() {
            this(null, null, null);
        }

        public ErrorHandlingEndpointArgs(String input_val_1, String input_val_2, Boolean throwManualError) {
            this.input_val_1 = input_val_1;
            this.input_val_2 = input_val_2;
            this.throwManualError = throwManualError;
        }
    }
}
