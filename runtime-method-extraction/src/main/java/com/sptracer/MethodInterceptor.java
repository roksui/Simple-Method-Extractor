package com.sptracer;

import com.sptracer.data.ClassDTO;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class MethodInterceptor {

    public static ClassDTO classDTO;

    @RuntimeType
    public static Object intercept(@Origin Method method, @Origin Class clazz, @SuperCall Callable<?> zuper) throws Exception {
        System.out.println("Method signature: " + method);
        System.out.println("This method is in class: " + clazz.getName());

        Tracer.start(method.getName());

        try {
            return zuper.call();
        } finally {
            Tracer.stop();
        }


//        // callCollector 인스턴스에 아직 해당 클래스가 존재하지 않으면
//        if (callCollector.containsClassName(clazz.getName())) {
//            classDTO = new ClassDTO(clazz.getName(), new ArrayList<>());
//            callCollector.add(classDTO);
//        }
//        callCollector.getClasses().get(callCollector.countClasses() - 1).addMethod(new MethodDTO(method.getName()));

    }
}