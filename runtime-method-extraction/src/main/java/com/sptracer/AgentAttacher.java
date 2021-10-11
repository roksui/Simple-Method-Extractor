package com.sptracer;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.instrument.Instrumentation;


public class AgentAttacher {

    private static final Logger logger = LoggerFactory.getLogger(AgentAttacher.class);
    private static Instrumentation instrumentation;

    private AgentAttacher() {
    }

    private static boolean initInstrumentation() {
        instrumentation = getInstrumentation();
        return true;
    }

    private static Instrumentation getInstrumentation() {
        return ByteBuddyAgent.getInstrumentation();
    }

    private static AgentBuilder createAgentBuilder() { // TODO: Memory Leak 관리하기
        return new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .ignore(ElementMatchers.nameStartsWith("com.sptracer"));
    }


}
