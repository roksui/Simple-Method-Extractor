package com.sptracer.configuration.converter;

public class ClassInstanceValueConverter<T> extends AbstractValueConverter<T> {

    private final Class<T> clazz;

    public static <T> ClassInstanceValueConverter<T> of(Class<T> clazz) {
        return new ClassInstanceValueConverter<T>(clazz);
    }

    private ClassInstanceValueConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T convert(String className) throws IllegalArgumentException {
        try {
            final Class<?> aClass = Class.forName(className);
            if (clazz.isAssignableFrom(aClass)) {
                @SuppressWarnings("unchecked") final Class<T> tClazz = (Class<T>) aClass;
                return tClazz.getConstructor().newInstance();
            } else {
                throw new IllegalArgumentException(aClass.getName() + " is not an instance of " + clazz.getName());
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Did not find a public no arg constructor for " + className, e);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public String toString(T value) {
        return value.getClass().getName();
    }
}
