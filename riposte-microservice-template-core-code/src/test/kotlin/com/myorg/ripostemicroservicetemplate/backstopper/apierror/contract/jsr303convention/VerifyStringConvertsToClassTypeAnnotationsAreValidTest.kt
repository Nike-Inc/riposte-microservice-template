package com.myorg.ripostemicroservicetemplate.backstopper.apierror.contract.jsr303convention

import com.nike.backstopper.apierror.contract.jsr303convention.ReflectionBasedJsr303AnnotationTrollerBase
import com.nike.backstopper.apierror.contract.jsr303convention.VerifyEnumsReferencedByStringConvertsToClassTypeJsr303AnnotationsAreJacksonCaseInsensitiveTest
import com.nike.backstopper.validation.constraints.StringConvertsToClassType

/**
 * Makes sure that any Enums referenced by [StringConvertsToClassType] JSR 303 annotations are case insensitive if
 * they are marked with [StringConvertsToClassType.allowCaseInsensitiveEnumMatch] set to true.
 *
 * You can exclude annotation declarations (e.g. for unit test classes that are intended to violate the naming
 * convention) by making sure that the
 * [ApplicationJsr303AnnotationTroller.ignoreAllAnnotationsAssociatedWithTheseProjectClasses] and
 * [ApplicationJsr303AnnotationTroller.specificAnnotationDeclarationExclusionsForProject] methods return
 * what you need, but you should not exclude any annotations in production code under normal circumstances.
 */
class VerifyStringConvertsToClassTypeAnnotationsAreValidTest :
    VerifyEnumsReferencedByStringConvertsToClassTypeJsr303AnnotationsAreJacksonCaseInsensitiveTest() {

    override fun getAnnotationTroller(): ReflectionBasedJsr303AnnotationTrollerBase {
        return ApplicationJsr303AnnotationTroller.instance
    }
}
