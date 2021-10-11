package com.sptracer.util;

public class ClassUtils {

    private ClassUtils() {
    }

    public static Class<?> forNameOrNull(String className) {
        try {
            return Class.forName(className, false, ClassUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static boolean isNotPresent(String className) {
        return !isPresent(className);
    }

    public static boolean isPresent(String className) {
        return forNameOrNull(className) != null;
    }

    public static boolean hasMethod(String className, String methodName, Class<?>... parameterTypes) {
        try {
            Class.forName(className).getMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean canLoadClass(ClassLoader loader, String className) {
        return loadClassOrReturnNull(loader, className) != null;
    }

    public static Class<?> loadClassOrReturnNull(ClassLoader loader, String className) {
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        try {
            return loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static String getIdentityString(Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(obj));
    }

    public static String shorten(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    }
}
