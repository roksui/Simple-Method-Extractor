package com.sptracer;

public interface Resetter<T> {

    /**
     * Recycles a pooled object state
     *
     * @param object object to recycle
     */
    void recycle(T object);

    /**
     * Resetter for objects that implement {@link Recyclable}
     *
     * @param <T> recyclable object type
     */
    class ForRecyclable<T extends Recyclable> implements Resetter<T> {
        private static ForRecyclable INSTANCE = new ForRecyclable();

        public static <T extends Recyclable> Resetter<T> get() {
            return INSTANCE;
        }

        @Override
        public void recycle(Recyclable object) {
            object.resetState();
        }
    }

}
