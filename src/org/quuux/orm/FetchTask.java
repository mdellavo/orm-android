package org.quuux.orm;

import java.util.List;

public class FetchTask<T> extends QueryTask {
    public FetchTask(final Connection connection, final Query query, final FetchListener<T> listener) {
        super(connection, query, new QueryListener<T>() {
            @Override
            public void onResult(final List<T> result) {
                if (result != null && result.size() > 0)
                    if (listener != null)
                        listener.onResult(result.get(0));
            }
        });
    }
}
