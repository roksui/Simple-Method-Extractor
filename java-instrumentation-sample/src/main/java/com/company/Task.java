package com.company;

public class Task {

    public Task() {
        System.out.println("This is constructor");
    }

    public void method1() {
        System.out.println("This is method 1");
    }

    public void method2() {
        System.out.println("This is method 2");
        method3(10);
    }

    public void method3(int number) {
        System.out.println("This is method 3 that has " + number + " as parameter");
    }

    public int add(int x, int y) {
        return x + y;
    }
}
