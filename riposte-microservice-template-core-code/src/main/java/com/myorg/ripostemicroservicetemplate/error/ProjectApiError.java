package com.myorg.ripostemicroservicetemplate.error;

import com.nike.backstopper.apierror.ApiError;
import com.nike.backstopper.apierror.ApiErrorBase;
import com.nike.internal.util.MapBuilder;

import com.myorg.ripostemicroservicetemplate.endpoints.ExampleEndpoint;

import java.util.Map;
import java.util.UUID;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

/**
 * Contains the application-specific errors that can occur. Each enum value maps an application-specific error to the
 * appropriate HTTP status code, and contains an appropriate human-readable message.
 *
 * <p>These can be manually thrown with {@link com.nike.backstopper.exception.ApiException}, or they can be used as the
 * {@code message} for any JSR 303 (Java Bean Validation) annotation (e.g. {@link
 * javax.validation.constraints.NotNull#message()} and if that violation is triggered it will be converted by the
 * Backstopper-powered error handling system into the appropriate error instance below.
 *
 * <p>NOTE: These codes are intended to be project-specific. Feel free to rename this class to something specific to the
 * project, e.g. [MyProject]ApiError.
 *
 * <p>TODO: EXAMPLE CLEANUP - Remove the EXAMPLE_* error enum values.
 */
public enum ProjectApiError implements ApiError {

    /**
     * These are just example errors. Each project should delete this and define their own project specific errors. But
     * take a look at how they are used in {@link ExampleEndpoint} first so you know how the validation and error
     * handling system works.
     */
    EXAMPLE_ERROR_BAD_INPUT_VAL_1(99150, "Bad request body - null/empty input_val_1", BAD_REQUEST.code()),
    EXAMPLE_ERROR_BAD_INPUT_VAL_2(99151, "Bad request body - null/empty input_val_2", BAD_REQUEST.code()),
    EXAMPLE_ERROR_MANUALLY_THROWN(99152, "You asked for an error to be thrown", INTERNAL_SERVER_ERROR.code(),
                                  MapBuilder.builder("static_metadata_1", (Object)"foo")
                                            .put("static_metadata_2", 42)
                                            .build()
    );

    private final ApiError delegate;

    ProjectApiError(ApiError delegate) {
        this.delegate = delegate;
    }

    ProjectApiError(int errorCode, String message, int httpStatusCode) {
        this(errorCode, message, httpStatusCode, null);
    }

    ProjectApiError(int errorCode, String message, int httpStatusCode, Map<String, Object> metadata) {
        this(new ApiErrorBase(
            "delegated-to-enum-wrapper-" + UUID.randomUUID().toString(), errorCode, message, httpStatusCode, metadata
        ));
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getErrorCode() {
        return delegate.getErrorCode();
    }

    @Override
    public String getMessage() {
        return delegate.getMessage();
    }

    @Override
    public Map<String, Object> getMetadata() {
        return delegate.getMetadata();
    }

    @Override
    public int getHttpStatusCode() {
        return delegate.getHttpStatusCode();
    }

}
