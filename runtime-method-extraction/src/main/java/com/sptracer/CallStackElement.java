package com.sptracer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CallStackElement {

    private static Queue<CallStackElement> objectPool;

    @JsonIgnore
    private CallStackElement parent;
    private String signature;
    private List<CallStackElement> children = new LinkedList<>();

    public static CallStackElement createRoot(String signature) {
        return CallStackElement.create(null, signature);
    }

    public static CallStackElement create(CallStackElement parent, String signature) {
        CallStackElement cse = new CallStackElement();

        cse.signature = signature;
        if (parent != null) {
            cse.parent = parent;
            parent.children.add(cse);
        }
        return cse;
    }

    public CallStackElement executionStopped() {
        parent.removeLastChild();

        return parent;
    }

    public List<CallStackElement> getChildren() {
        return children;
    }

    public void setChildren(List<CallStackElement> children) {
        this.children = children;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public CallStackElement getParent() {
        return parent;
    }

    public void setParent(CallStackElement parent) {
        this.parent = parent;
    }

    private void removeLastChild() {
        children.remove(children.size() - 1);
    }

}
