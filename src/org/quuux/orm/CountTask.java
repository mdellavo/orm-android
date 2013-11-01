package org.quuux.orm;


import android.database.Cursor;
import android.os.AsyncTask;

import java.util.List;

public class CountTask extends ScalarTask {

    public CountTask(final Connection connection, final Query query, final CountListener listener) {
        super(connection, query.count(), new ScalarListener() {
            @Override
            public void onResult(final Object obj) {
                listener.onResult((Long) obj);
            }
        });
    }
}