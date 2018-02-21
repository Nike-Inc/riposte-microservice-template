package com.myorg.ripostemicroservicetemplate.endpoints

import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint.Post
import com.myorg.ripostemicroservicetemplate.error.ProjectApiError
import com.nike.backstopper.apierror.ApiErrorWithMetadata
import com.nike.backstopper.exception.ApiException
import com.nike.backstopper.handler.riposte.config.guice.BackstopperRiposteConfigGuiceModule
import com.nike.internal.util.Pair
import com.nike.riposte.server.http.RequestInfo
import com.nike.riposte.server.http.ResponseInfo
import com.nike.riposte.server.http.StandardEndpoint
import com.nike.riposte.util.Matcher
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpMethod
import io.netty.handler.codec.http.HttpResponseStatus
import org.hibernate.validator.constraints.NotBlank
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import javax.validation.constraints.NotNull

/**
 * An example endpoint that shows how to do automatic and manual validation (in the [Post] inner class).
 *
 * The automatic validation is done because [Post.isValidateRequestContent] defaults to true and
 * the [Post.requestContentType] method inherited from [StandardEndpoint] returns a non-null value
 * (subclasses of [StandardEndpoint] are able to infer the input type automatically based on the generic type
 * declarations on the class). If either of those things were not true then the deserialized request content would not
 * automatically be run through the validator. The validator runs JSR 303 validation on the request's
 * [RequestInfo.getContent] before calling the execute method. See the [ExampleEndpoint.ErrorHandlingEndpointArgs]
 * class for an example of how to annotate classes that you want to be JSR 303 validated.
 *
 * The manual validation is performed in the execute method by manually throwing an [ApiException].
 *
 * This validation and error handling system is powered by
 * [Backstopper](https://github.com/Nike-Inc/backstopper). See that project's readme for more information.
 *
 * TODO: EXAMPLE CLEANUP - Delete this class.
 *
 * @author Nic Munroe
 */
object ExampleEndpoint {

    const val MATCHING_PATH = "/example"

    /**
     * The GET implementation of /example
     */
    class Get : StandardEndpoint<Void, ErrorHandlingEndpointArgs>() {

        private val matcher: Matcher = Matcher.match(MATCHING_PATH, HttpMethod.GET)

        override fun execute(request: RequestInfo<Void>,
                             longRunningTaskExecutor: Executor,
                             ctx: ChannelHandlerContext): CompletableFuture<ResponseInfo<ErrorHandlingEndpointArgs>> {
            // Since we're not doing anything time consuming we don't need to execute anything on another thread and we
            //      can just return an already-completed CompletableFuture.
            return CompletableFuture.completedFuture(
                    ResponseInfo.newBuilder(
                            ErrorHandlingEndpointArgs(
                                    "some-val1-" + UUID.randomUUID().toString(),
                                    "some-val2-" + UUID.randomUUID().toString(),
                                    false
                            )
                    ).build()
            )
        }

        override fun requestMatcher(): Matcher {
            return matcher
        }

    }

    /**
     * The POST implementation of /example
     */
    class Post : StandardEndpoint<ErrorHandlingEndpointArgs, ErrorHandlingEndpointArgs>() {

        private val matcher: Matcher = Matcher.match(MATCHING_PATH, HttpMethod.POST)

        /**
         * Resource endpoint that gives an example of how to use the error handling system (hooked up to Backstopper via
         * [BackstopperRiposteConfigGuiceModule] Guice module) to handle all the errors in your application, both
         * for object validation via JSR 303 annotations and manually thrown errors.
         *
         * @param request
         * The incoming request. [RequestInfo.getContent] contains the request body with the arguments that
         * the client can pass (some are required, others are not - see the JSR 303 validation annotations on the
         * [ErrorHandlingEndpointArgs] class to see which are which).
         */
        override fun execute(
                request: RequestInfo<ErrorHandlingEndpointArgs>,
                longRunningTaskExecutor: Executor,
                ctx: ChannelHandlerContext
        ): CompletableFuture<ResponseInfo<ErrorHandlingEndpointArgs>> {
            // If we reach here then the request content has already been run through the JSR 303 validator and we know
            //      it's non-null (since the InputType in our StandardEndpoint<InputType, OutputType> definition is
            //      not Void).
            val content: ErrorHandlingEndpointArgs = request.content

            // Manually check the throwManualError query param (normally you'd do this with JSR 303 annotations on the
            //      object, but this shows how you can manually throw exceptions to be picked up by the error handling
            //      system).
            if (java.lang.Boolean.TRUE == content.throwManualError) {
                throw ApiException.newBuilder()
                        .withExceptionMessage("Manual error throw was requested")
                        .withApiErrors(ApiErrorWithMetadata(
                                ProjectApiError.EXAMPLE_ERROR_MANUALLY_THROWN,
                                Pair.of("dynamic_metadata", System.currentTimeMillis() as Any)
                        ))
                        .withExtraDetailsForLogging(Pair.of("some_important_log_info", "foo"))
                        .withExtraResponseHeaders(
                                Pair.of("useful-error-related-response-header", listOf("foo"))
                        )
                        .build()
            }

            // Since we're not doing anything time consuming we don't need to execute anything on another thread and we
            //      can just return an already-completed CompletableFuture.
            return CompletableFuture.completedFuture(
                    ResponseInfo.newBuilder(content).withHttpStatusCode(HttpResponseStatus.CREATED.code()).build()
            )
        }

        override fun requestMatcher(): Matcher {
            return matcher
        }

    }

    class ErrorHandlingEndpointArgs(
            @field:NotNull(message = "EXAMPLE_ERROR_BAD_INPUT_VAL_1")
            @field:NotBlank(message = "EXAMPLE_ERROR_BAD_INPUT_VAL_1")
            val input_val_1: String?,

            @field:NotNull(message = "EXAMPLE_ERROR_BAD_INPUT_VAL_2")
            @field:NotBlank(message = "EXAMPLE_ERROR_BAD_INPUT_VAL_2")
            val input_val_2: String?,

            val throwManualError: Boolean?
    )
}
