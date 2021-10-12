package com.sptracer;

import com.sun.istack.internal.Nullable;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.security.ProtectionDomain;
import java.util.Collection;

import static net.bytebuddy.matcher.ElementMatchers.any;

public abstract class SpTracerInstrumentation {
    /**
     * Pre-select candidates solely based on the class name for the slower {@link #getTypeMatcher()},
     * at the expense of potential false negative matches.
     * <p>
     * Any matcher which does not only take the class name into account,
     * causes the class' bytecode to be parsed.
     * If the matcher needs information from other classes than the one currently being loaded,
     * like it's super class,
     * those classes have to be loaded from the file system,
     * unless they are cached or already loaded.
     * </p>
     */
    public ElementMatcher<? super NamedElement> getTypeMatcherPreFilter() {
        return any();
    }

    /**
     * Post filters classes that pass the {@link #getTypeMatcher()} by {@link ProtectionDomain}.
     */
    public ElementMatcher.Junction<ProtectionDomain> getProtectionDomainPostFilter() {
        return any();
    }

    /**
     * The type matcher selects types which should be instrumented by this advice
     * <p>
     * To make type matching more efficient,
     * first apply the cheaper matchers like {@link ElementMatchers#nameStartsWith(String)} and {@link ElementMatchers#isInterface()}
     * which pre-select the types as narrow as possible.
     * Only then use more expensive matchers like {@link ElementMatchers#hasSuperType(ElementMatcher)}
     * </p>
     *
     * @return the type matcher
     */
    public abstract ElementMatcher<? super TypeDescription> getTypeMatcher();

    public ElementMatcher.Junction<ClassLoader> getClassLoaderMatcher() {
        return any();
    }

    /**
     * The method matcher selects methods of types matching {@link #getTypeMatcher()},
     * which should be instrumented
     *
     * @return the method matcher
     */
    public abstract ElementMatcher<? super MethodDescription> getMethodMatcher();

    /**
     * Implementing the advice and instrumentation at the same class is <b>disallowed</b> and will throw a validation error when trying to do so.
     * They are loaded in different contexts with different purposes. The instrumentation class is loaded by the agent class
     * loader, whereas the advice class needs to be loaded by a class loader that has visibility to the instrumented
     * type and library, as well as the agent classes. Therefore, loading the advice class through the agent class
     * loader may cause linkage-related errors.
     * <p>
     *     ANY INSTRUMENTATION THAT OVERRIDES THIS METHOD MUST NOT CAUSE THE LOADING OF THE ADVICE CLASS.
     *     For example, implementing it as {@code MyAdvice.class.getName()} is not allowed.
     * </p>
     * @return the name of the advice class corresponding this instrumentation
     */
    public String getAdviceClassName() {
        return getClass().getName() + "$AdviceClass";
    }

    /**
     * Returns {@code true} if this instrumentation should be applied even when {@code instrument} is set to {@code false}.
     */
    public boolean includeWhenInstrumentationIsDisabled() {
        return false;
    }

    /**
     * Returns a name which groups several instrumentations into a logical group.
     * <p>
     * This name is used in {@code disabled_instrumentations} to exclude a logical group
     * of instrumentations.
     * </p>
     *
     * @return a name which groups several instrumentations into a logical group
     */
    public abstract Collection<String> getInstrumentationGroupNames();

    @Nullable
    public Advice.OffsetMapping.Factory<?> getOffsetMapping() {
        return null;
    }

    public void onTypeMatch(TypeDescription typeDescription, ClassLoader classLoader, ProtectionDomain protectionDomain, @Nullable Class<?> classBeingRedefined) {
    }

}
