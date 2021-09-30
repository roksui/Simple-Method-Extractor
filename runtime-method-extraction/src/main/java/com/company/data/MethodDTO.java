package com.company.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 출력용 Method DTO
 */
public class MethodDTO {
    @JsonProperty
    protected String name;
    @JsonProperty
    protected List<String> parameters;

    public MethodDTO() {
        this.name = "";
        this.parameters = new ArrayList<>();
    }

    public MethodDTO(String name, List<String> parameters) {
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
