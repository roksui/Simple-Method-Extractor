package com.company;

import com.company.data.ClassDTO;
import com.company.data.MethodDTO;
import net.bytebuddy.asm.Advice;
import java.util.ArrayList;

public class MethodInterceptor {

    private static final CallCollector callCollector = CallCollector.getInstance();
    private static ClassDTO classDTO;

    @Advice.OnMethodEnter
    static void enterMethods(@Advice.Origin String method, @Advice.Origin Class<?> clazz) throws Exception {
        System.out.println(method);

        System.out.println("method of class: " + clazz.getName());

        // callCollector 인스턴스에 아직 해당 클래스가 존재하지 않으면
        if (!callCollector.containsClassName(clazz.getName())) {
            classDTO = new ClassDTO(clazz.getName(), new ArrayList<>());
            callCollector.add(classDTO);
        }
        callCollector.getClasses().get(callCollector.countClasses() - 1).addMethod(new MethodDTO(method));
    }
}