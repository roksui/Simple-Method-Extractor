package com.sptracer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/*
*
 * 실행 중인 프로그램의 모든 메소드를 가로채는 Agent 클래스
public class Agent {

    public static void premain(String arg, Instrumentation instrumentation) {

        System.out.println("Entering premain... >> Agent for extracting all the methods");

        new AgentBuilder.Default()
                .ignore(ElementMatchers.nameStartsWith("com.sptracer"))
                .type((ElementMatchers.any()))
                .transform((builder, typeDescription, classLoader, module) -> builder
                        .constructor(ElementMatchers.any())
                        .intercept(Advice.to(ConstructorInterceptor.class))
                        .method(ElementMatchers.any())
                        .intercept(MethodDelegation.to(MethodInterceptor.class)))
                .installOn(instrumentation);
    }
}*/
