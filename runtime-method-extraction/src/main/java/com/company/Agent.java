package com.company;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

/**
 * 실행 중인 프로그램의 모든 메소드를 가로채는 Agent 클래스
 */
public class Agent {

    public static void premain(String arg, Instrumentation instrumentation) {

        System.out.println("Entering premain... >> Agent for extracting all the methods");

        new AgentBuilder.Default()
                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
                .type((ElementMatchers.any()))
                .transform((builder, typeDescription, classLoader, module) -> builder
                        // constructor를 intercept하기 위해서는 아래 줄과 같이 Advice를 따로 만들어야 함
                        .constructor(ElementMatchers.any())
                        .intercept(Advice.to(Constructor.class))
                        .method(ElementMatchers.any())
                        .intercept(Advice.to(Methods.class))
                ).installOn(instrumentation);

        /*
        OR visit을 통해 agent를 사용할 수 있다. constructor와 method 모두 intercept 한다.

        new AgentBuilder.Default()
                .with(new AgentBuilder.InitializationStrategy.SelfInjection.Eager())
                .type((ElementMatchers.nameContains("Methods")))
                .transform((builder, typeDescription, classLoader, module) -> builder
                        .visit(Advice.to(Methods.class).on(ElementMatchers.any()))
                ).installOn(instrumentation);
         */
    }
}