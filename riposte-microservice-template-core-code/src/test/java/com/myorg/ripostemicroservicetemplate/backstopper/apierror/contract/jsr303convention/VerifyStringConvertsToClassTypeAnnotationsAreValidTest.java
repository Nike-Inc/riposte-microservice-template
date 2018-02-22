package com.myorg.ripostemicroservicetemplate.backstopper.apierror.contract.jsr303convention;

import com.nike.backstopper.apierror.contract.jsr303convention.ReflectionBasedJsr303AnnotationTrollerBase;
import com.nike.backstopper.apierror.contract.jsr303convention.VerifyEnumsReferencedByStringConvertsToClassTypeJsr303AnnotationsAreJacksonCaseInsensitiveTest;
import com.nike.backstopper.validation.constraints.StringConvertsToClassType;

/**
 * Makes sure that any Enums referenced by {@link StringConvertsToClassType} JSR 303 annotations are case insensitive if
 * they are marked with {@link StringConvertsToClassType#allowCaseInsensitiveEnumMatch()} set to true.
 *
 * <p>You can exclude annotation declarations (e.g. for unit test classes that are intended to violate the naming
 * convention) by making sure that the {@link ApplicationJsr303AnnotationTroller#ignoreAllAnnotationsAssociatedWithTheseProjectClasses()}
 * and {@link ApplicationJsr303AnnotationTroller#specificAnnotationDeclarationExclusionsForProject()} methods return
 * what you need, but you should not exclude any annotations in production code under normal circumstances.
 *
 * @author Nic Munroe
 */
public class VerifyStringConvertsToClassTypeAnnotationsAreValidTest
    extends VerifyEnumsReferencedByStringConvertsToClassTypeJsr303AnnotationsAreJacksonCaseInsensitiveTest {

    @Override
    protected ReflectionBasedJsr303AnnotationTrollerBase getAnnotationTroller() {
        return ApplicationJsr303AnnotationTroller.INSTANCE;
    }
}
