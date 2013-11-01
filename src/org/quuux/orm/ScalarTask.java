package org.quuux.orm;

import android.database.Cursor;
import android.os.AsyncTask;

public class ScalarTask extends AsyncTask<Void, Void, Long> {


    private final Query mQuery;
    private final Connection mConnection;
    private final ScalarListener mListener;

    public ScalarTask(final Connection connection, final Query query, final ScalarListener listener) {
        mConnection = connection;
        mQuery = query;
        mListener = listener;
    }

    private Cursor query() {
        final String sql = mQuery.toSql();
        final String[] args = mQuery.getSelectionArgs() != null && mQuery.getSelectionArgs().length > 0 ? SchemaBuilder.flattenArgs(mQuery.getSelectionArgs()) : null;
        return mConnection.query(sql, args);
    }


    @Override
    protected Long doInBackground(final Void... params) {
        final Cursor cursor = query();
        return cursor.moveToFirst() ? cursor.getLong(0) : -1;
    }

    @Override
    protected void onPostExecute(final Long count) {
        super.onPostExecute(count);
        if (mListener != null)
            mListener.onResult(count);
    }
}
