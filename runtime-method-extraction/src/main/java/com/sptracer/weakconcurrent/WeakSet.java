package com.sptracer.weakconcurrent;

public interface WeakSet<E> extends Iterable<E> {

    boolean add(E element);

    boolean contains(E element);

    boolean remove(E element);
}
