package com.myorg.ripostemicroservicetemplate.backstopper.apierror.contract.jsr303convention

import com.myorg.ripostemicroservicetemplate.error.ProjectApiErrorsImpl
import com.nike.backstopper.apierror.contract.jsr303convention.ReflectionBasedJsr303AnnotationTrollerBase
import com.nike.backstopper.apierror.contract.jsr303convention.VerifyJsr303ValidationMessagesPointToApiErrorsTest
import com.nike.backstopper.apierror.projectspecificinfo.ProjectApiErrors

/**
 * Verifies that *ALL* non-excluded JSR 303 validation annotations in this project have a message defined that maps to
 * a [com.nike.backstopper.apierror.ApiError] enum name from this project's [ProjectApiErrors]. This is how
 * the JSR 303 Bean Validation system is connected to the Backstopper error handling system and you should NOT disable
 * these tests.
 *
 * You can exclude annotation declarations (e.g. for unit test classes that are intended to violate the naming
 * convention) by making sure that the
 * [ApplicationJsr303AnnotationTroller.ignoreAllAnnotationsAssociatedWithTheseProjectClasses] and
 * [ApplicationJsr303AnnotationTroller.specificAnnotationDeclarationExclusionsForProject] methods return
 * what you need, but you should not exclude any annotations in production code under normal circumstances.
 *
 * @author Nic Munroe
 */
class VerifyJsr303ContractTest : VerifyJsr303ValidationMessagesPointToApiErrorsTest() {

    override fun getAnnotationTroller(): ReflectionBasedJsr303AnnotationTrollerBase {
        return ApplicationJsr303AnnotationTroller.instance
    }

    override fun getProjectApiErrors(): ProjectApiErrors {
        return PROJECT_API_ERRORS
    }

    companion object {
        private val PROJECT_API_ERRORS = ProjectApiErrorsImpl()
    }
}
