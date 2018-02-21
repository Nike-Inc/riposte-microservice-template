package com.myorg.ripostemicroservicetemplate.backstopper.apierror.contract.jsr303convention

import com.google.common.base.Predicate
import com.nike.backstopper.apierror.contract.jsr303convention.ReflectionBasedJsr303AnnotationTrollerBase
import com.nike.internal.util.Pair
import java.lang.reflect.AnnotatedElement

/**
 * Extension of [ReflectionBasedJsr303AnnotationTrollerBase] for use with this project. This is used by JSR 303
 * annotation convention enforcement tests (e.g. [VerifyJsr303ContractTest] and
 * [VerifyStringConvertsToClassTypeAnnotationsAreValidTest]).
 *
 * NOTE: If you want to exclude classes or specific JSR 303 annotations from triggering convention violation errors
 * in those tests you can do so by populating the return values of the
 * [ignoreAllAnnotationsAssociatedWithTheseProjectClasses] and [specificAnnotationDeclarationExclusionsForProject]
 * methods. **IMPORTANT** - this should only be done if you *really* know what you're doing. Usually it's only done for
 * unit test classes that are intended to violate the convention. It should not be done for production code under
 * normal circumstances. See the javadocs for the super class for those methods if you need to use them.
 *
 * @author Nic Munroe
 */
class ApplicationJsr303AnnotationTroller
// Intentionally private - use {@code getInstance()} to retrieve the singleton instance of this class.
private constructor() : ReflectionBasedJsr303AnnotationTrollerBase() {

    public override fun ignoreAllAnnotationsAssociatedWithTheseProjectClasses(): List<Class<*>>? {
        return null
    }

    public override fun specificAnnotationDeclarationExclusionsForProject(): List<Predicate<Pair<Annotation, AnnotatedElement>>>? {
        return null
    }

    companion object {
        val instance = ApplicationJsr303AnnotationTroller()
    }
}
