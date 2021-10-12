package com.sptracer.matcher;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class MethodHierarchyMatcher extends ElementMatcher.Junction.AbstractBase<MethodDescription> {

    private final ElementMatcher<? super MethodDescription> extraMethodMatcher;
    private final ElementMatcher<? super TypeDescription> superClassMatcher;

    MethodHierarchyMatcher(ElementMatcher<? super MethodDescription> extraMethodMatcher) {
        this(extraMethodMatcher, not(is(TypeDescription.ForLoadedType.OBJECT)));
    }

    private MethodHierarchyMatcher(ElementMatcher<? super MethodDescription> extraMethodMatcher, ElementMatcher<? super TypeDescription> superClassMatcher) {
        this.extraMethodMatcher = extraMethodMatcher;
        this.superClassMatcher = superClassMatcher;
    }

    public ElementMatcher<MethodDescription> onSuperClassesThat(ElementMatcher<? super TypeDescription> superClassMatcher) {
        return new MethodHierarchyMatcher(extraMethodMatcher, superClassMatcher);
    }

    @Override
    public boolean matches(MethodDescription targetMethod) {
        return declaresInHierarchy(targetMethod, targetMethod.getDeclaringType().asErasure());
    }

    private boolean declaresInHierarchy(MethodDescription targetMethod, TypeDescription type) {
        if (declaresMethod(named(targetMethod.getName())
                .and(returns(targetMethod.getReturnType().asErasure()))
                .and(takesArguments(targetMethod.getParameters().asTypeList().asErasures()))
                .and(extraMethodMatcher))
                .matches(type)) {
            return true;
        }
        for (TypeDescription interfaze : type.getInterfaces().asErasures()) {
            if (superClassMatcher.matches(interfaze)) {
                if (declaresInHierarchy(targetMethod, interfaze)) {
                    return true;
                }
            }
        }
        final TypeDescription.Generic superClass = type.getSuperClass();
        if (superClass != null && superClassMatcher.matches(superClass.asErasure())) {
            return declaresInHierarchy(targetMethod, superClass.asErasure());
        }
        return false;
    }

}
