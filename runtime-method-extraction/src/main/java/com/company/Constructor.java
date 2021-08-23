package com.company;

import net.bytebuddy.asm.Advice;

public class Constructor {

    @Advice.OnMethodEnter
    static void enterConstructor(@Advice.Origin String method) throws Exception {

        System.out.println(method);
    }
}
