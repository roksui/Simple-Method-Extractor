package com.company;

import net.bytebuddy.asm.Advice;

public class Methods {

    @Advice.OnMethodEnter
    static void enterMethods(@Advice.Origin String method) throws Exception {

        System.out.println(method);
    }
}
