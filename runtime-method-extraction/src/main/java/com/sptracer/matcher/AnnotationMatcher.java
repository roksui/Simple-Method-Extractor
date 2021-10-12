package com.sptracer.matcher;

import net.bytebuddy.description.annotation.AnnotationSource;
import net.bytebuddy.matcher.ElementMatcher;

import javax.annotation.Nullable;

import static net.bytebuddy.matcher.ElementMatchers.isAnnotatedWith;

/**
 * ByteBuddy ElementMatcher that finds elements that are annotated with matching annotations.
 * <p>
 * The matcher must be initialized with a pattern prefixed with '@' or '@@' to find either annotation names or meta annotation names.
 * <p>
 * If prefixed with '@' it matches {@link AnnotationSource}s that are annotated with an annotation whose name matches the pattern.
 * If prefixed with '@@' it matches {@link AnnotationSource}s that are annotated with annotations that are themselves annotated with an annotation whose name matches the pattern.
 * The pattern itself is matched against the name of an annotation using {@link WildcardMatcher}
 * <p>
 * Examples:
 * <ul>
 * <li>@java.inject.ApplicationScoped matches all types annotated with the java.inject.ApplicationScoped annotation</li>
 * <li>@java.inject.* matches all types annotated with any annotation in the 'javax.inject' package or subpackages</li>
 * <li>@@javax.enterprise.context.NormalScope matches all typed annotated with an annotation that is annotated with the javax.enterprise.context.NormalScope annotation</li>
 * </ul>
 *
 * @see WildcardMatcher
 */
public class AnnotationMatcher extends ElementMatcher.Junction.AbstractBase<AnnotationSource> {


    @Nullable
    private final WildcardMatcher wildcardMatcher;
    private final boolean metaAnnotation;
    private final String annotationPattern;

    private AnnotationMatcher(String annotationPattern) {
        if (!annotationPattern.startsWith("@")) {
            throw new IllegalArgumentException("Invalid annotation pattern: " + annotationPattern);
        }
        this.annotationPattern = annotationPattern;
        this.metaAnnotation = annotationPattern.startsWith("@@");
        String wildcardPattern = annotationPattern.substring(metaAnnotation ? 2 : 1);
        this.wildcardMatcher = WildcardMatcher.valueOf(wildcardPattern);
    }

    private AnnotationMatcher() {
        wildcardMatcher = null;
        metaAnnotation = false;
        annotationPattern = "*";
    }

    /**
     * @param annotationPattern the pattern to match against. Must either start with '@' or '@@'.
     * @return an AnnotationMatcher for the specified annotationPattern
     */
    public static AnnotationMatcher annotationMatcher(String annotationPattern) {
        return new AnnotationMatcher(annotationPattern);
    }

    /**
     * @return an AnnotationMatcher that always returns true (matches anything)
     */
    public static AnnotationMatcher matchAll() {
        return new AnnotationMatcher();
    }

    /**
     * Test if the target has any annotations matching the annotationPattern of this matcher.
     *
     * @param target the AnnotationSource to test.
     * @return true if the target has an annotation matching the annotationPattern of this AnnotationMatcher, else false.
     * If this AnnotationMatcher was initialized with {@link #matchAll()} always returns true.
     */
    public boolean matches(AnnotationSource target) {
        if (matchesAll()) {
            return true;
        }

        ElementMatcher.Junction<AnnotationSource> resultMatcher = isAnnotatedWith(CustomElementMatchers.matches(wildcardMatcher));
        if (metaAnnotation) {
            resultMatcher = isAnnotatedWith(resultMatcher);
        }
        return resultMatcher.matches(target);
    }

    @Override
    public String toString() {
        return "matches(" + annotationPattern + ")";
    }

    private boolean matchesAll() {
        return wildcardMatcher == null;
    }
}
