package com.company;

import com.company.data.ClassDTO;

import java.util.ArrayList;
import java.util.List;

public class CallCollector {

    private List<ClassDTO> classes;

    private static CallCollector instance = new CallCollector();

    private CallCollector() {
        classes = new ArrayList<>();
    }

    public static CallCollector getInstance() {
        return instance;
    }

    public void add(final ClassDTO classDTO) {
        classes.add(classDTO);
    }

    public List<ClassDTO> getClasses() {
        return classes;
    }

    public int countClasses() {
        return classes.size();
    }

    public boolean containsClassName(String className) {
        for (ClassDTO classDTO : classes) {
            if (classDTO.getClassFQN().equals(className)) {
                return true;
            }
        }
        return false;
    }
}
