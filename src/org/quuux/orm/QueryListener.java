package org.quuux.orm;

import java.util.List;

public interface QueryListener<T> {
    public void onResult(List<T> result);
}
