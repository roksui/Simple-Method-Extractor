package com.sptracer.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 출력용 Method DTO
 */
public class MethodDTO {
    @JsonProperty
    private String modifiers;
    @JsonProperty
    private String returnType;
    @JsonProperty
    private String name;
    @JsonProperty
    private List<String> parameters;

    public MethodDTO() {
        this.name = "";
        this.parameters = new ArrayList<>();
    }

    public MethodDTO(String name) {
        this.modifiers = "";
        this.returnType = "";
        this.name = name;
        this.parameters = new ArrayList<>();
    }

    public MethodDTO(String modifiers, String returnType, String name, List<String> parameters) {
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public void addParameters(String parameter) {
        this.parameters.add(parameter);
    }
}
