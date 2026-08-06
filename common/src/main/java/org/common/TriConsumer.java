package org.common;

public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
}