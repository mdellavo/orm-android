package org.quuux.orm;

import java.util.List;

public class FetchTask<T> extends QueryTask {
    public FetchTask(final Connection connection, final Query query, final FetchListener<T> listener) {
        super(connection, query.limit(1), new QueryListener<T>() {
            @Override
            public void onResult(final List<T> result) {
                T rv = result != null && result.size() > 0 ? result.get(0) : null;
                if (listener != null)
                    listener.onResult(rv);
            }
        });
    }
}
