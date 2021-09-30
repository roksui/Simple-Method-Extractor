package com.company;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class CallCollector {

    private static CallCollector callCollector = new CallCollector();

    private List<Signature> calls = new ArrayList<>();

    private CallCollector() {
    }

    public static CallCollector instance() {
        return callCollector;
    }

    public void add(final Signature signature) {
        calls.add(signature);
    }
}
