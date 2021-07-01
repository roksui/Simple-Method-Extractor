package com.company;

/**
 * 메소드 시그니쳐를 담는 POJO
 */
public class Signature {

    private String modifiers;
    private String returnType;
    private String className;
    private String method;
    private String signature;

    public Signature() {

    }

    public Signature(String modifiers, String returnType, String className, String method, String signature) {
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.className = className;
        this.method = method;
        this.signature = signature;
    }

    public String getModifiers() {
        return modifiers;
    }

    public void setModifiers(String modifiers) {
        this.modifiers = modifiers;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
