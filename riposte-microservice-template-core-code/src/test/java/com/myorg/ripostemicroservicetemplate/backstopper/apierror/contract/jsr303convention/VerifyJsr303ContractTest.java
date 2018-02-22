package com.myorg.ripostemicroservicetemplate.backstopper.apierror.contract.jsr303convention;

import com.nike.backstopper.apierror.contract.jsr303convention.ReflectionBasedJsr303AnnotationTrollerBase;
import com.nike.backstopper.apierror.contract.jsr303convention.VerifyJsr303ValidationMessagesPointToApiErrorsTest;
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors;

import com.myorg.ripostemicroservicetemplate.error.ProjectApiErrorsImpl;

/**
 * Verifies that *ALL* non-excluded JSR 303 validation annotations in this project have a message defined that maps to a
 * {@link com.nike.backstopper.apierror.ApiError} enum name from this project's {@link ProjectApiErrors}. This is how
 * the JSR 303 Bean Validation system is connected to the Backstopper error handling system and you should NOT disable
 * these tests.
 *
 * <p>You can exclude annotation declarations (e.g. for unit test classes that are intended to violate the naming
 * convention) by making sure that the {@link ApplicationJsr303AnnotationTroller#ignoreAllAnnotationsAssociatedWithTheseProjectClasses()}
 * and {@link ApplicationJsr303AnnotationTroller#specificAnnotationDeclarationExclusionsForProject()} methods return
 * what you need, but you should not exclude any annotations in production code under normal circumstances.
 *
 * @author Nic Munroe
 */
public class VerifyJsr303ContractTest extends VerifyJsr303ValidationMessagesPointToApiErrorsTest {

    private static final ProjectApiErrors PROJECT_API_ERRORS = new ProjectApiErrorsImpl();

    @Override
    protected ReflectionBasedJsr303AnnotationTrollerBase getAnnotationTroller() {
        return ApplicationJsr303AnnotationTroller.INSTANCE;
    }

    @Override
    protected ProjectApiErrors getProjectApiErrors() {
        return PROJECT_API_ERRORS;
    }
}
