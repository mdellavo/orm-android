package org.quuux.orm;

public interface ScalarListener<T> {
    void onResult(T obj);
}