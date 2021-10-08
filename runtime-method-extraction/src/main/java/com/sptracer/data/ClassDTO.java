package com.sptracer.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class ClassDTO {
    @JsonProperty
    protected String classFQN;
    @JsonProperty
    protected List<MethodDTO> methods;

    public ClassDTO() {
        methods = new ArrayList<>();
    }

    public ClassDTO(String classFQN, List<MethodDTO> methods) {
        this.classFQN = classFQN;
        this.methods = methods;
    }

    public void setClassFQN(String classFQN) {
        this.classFQN = classFQN;
    }

    public String getClassFQN() {
        return classFQN;
    }

    public void setMethods(List<MethodDTO> methods) {
        this.methods = methods;
    }

    public void addMethod(MethodDTO methodDTO) {
        this.methods.add(methodDTO);
    }
}
