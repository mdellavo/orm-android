package org.quuux.orm;

public interface FetchListener<T> {
    public void onResult(T result);
}
