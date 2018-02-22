package com.myorg.ripostemicroservicetemplate.backstopper.apierror.contract.jsr303convention;

import com.nike.backstopper.apierror.contract.jsr303convention.ReflectionBasedJsr303AnnotationTrollerBase;
import com.nike.internal.util.Pair;

import com.google.common.base.Predicate;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * Extension of {@link ReflectionBasedJsr303AnnotationTrollerBase} for use with this project. This is used by JSR 303
 * annotation convention enforcement tests (e.g. {@link VerifyJsr303ContractTest} and {@link
 * VerifyStringConvertsToClassTypeAnnotationsAreValidTest}).
 *
 * <p>NOTE: If you want to exclude classes or specific JSR 303 annotations from triggering convention violation errors
 * in those tests you can do so by populating the return values of the {@link #ignoreAllAnnotationsAssociatedWithTheseProjectClasses()}
 * and {@link #specificAnnotationDeclarationExclusionsForProject()} methods. <b>IMPORTANT</b> - this should only be done
 * if you *really* know what you're doing. Usually it's only done for unit test classes that are intended to violate the
 * convention. It should not be done for production code under normal circumstances. See the javadocs for the super
 * class for those methods if you need to use them.
 *
 * @author Nic Munroe
 */
public final class ApplicationJsr303AnnotationTroller extends ReflectionBasedJsr303AnnotationTrollerBase {

    public static final ApplicationJsr303AnnotationTroller INSTANCE = new ApplicationJsr303AnnotationTroller();

    // Intentionally private - use {@code getInstance()} to retrieve the singleton instance of this class.
    private ApplicationJsr303AnnotationTroller() {
        super();
    }

    @Override
    protected List<Class<?>> ignoreAllAnnotationsAssociatedWithTheseProjectClasses() {
        return null;
    }

    @Override
    protected List<Predicate<Pair<Annotation, AnnotatedElement>>> specificAnnotationDeclarationExclusionsForProject() {
        return null;
    }
}
