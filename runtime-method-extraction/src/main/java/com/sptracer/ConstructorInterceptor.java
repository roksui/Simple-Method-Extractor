package com.sptracer;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Constructor;

public class ConstructorInterceptor {

    @Advice.OnMethodExit
    public static void intercept(@Advice.Origin Constructor<?> constructor, @Advice.Origin Class clazz) throws Exception {
        System.out.println("Method signature(Constructor): " + constructor);
        System.out.println("This method is in class: " + clazz.getName());

    }
}
