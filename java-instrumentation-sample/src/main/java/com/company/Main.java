package com.company;

public class Main {

    public Main() {
    }

    public static void main(String[] args) {
        System.out.println("This is Target Project main method");

        Main m = new Main();
        m.run();

        int a = 1;
        int b = 2;

        Task task = new Task();
        task.method1();
        task.method2();
        task.add(1, 2);

        Cart.printItem("apple");
    }

    public void run() {
        // Basically do nothing.
        int i = 9;
    }

    public static class Cart {
        String[] items = null;

        public static void printItem(String item) {
            System.out.println(item);
        }
    }
}