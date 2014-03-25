package org.quuux.orm;

import android.database.Cursor;
import android.os.AsyncTask;


public class ScalarTask extends AsyncTask<Class, Void, Object> {


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

    protected Connection getConnection() {
        return mConnection;
    }

    protected Query getQuery() {
        return mQuery;
    }

    @Override
    protected Object doInBackground(final Class... params) {
        final Cursor cursor = query();

        Log.d("XXX", "num rows = %s", cursor.getCount());

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        final Class klass = params[0];

        Object rv = null;
        if (klass == Boolean.class || klass == boolean.class) {
            rv = cursor.getInt(0);
        } else if(klass == Integer.class || klass == int.class) {
           rv = cursor.getInt(0);
        } else if (klass == Long.class || klass == long.class) {
            rv = cursor.getLong(0);
        } else if (klass == Float.class || klass == float.class) {
            rv = cursor.getFloat(0);
        } else if (klass == Double.class || klass == double.class) {
            rv = cursor.getDouble(0);
        } else if (klass == Character.class || klass == char.class) {
            rv = cursor.getString(0).charAt(0);
        } else if (klass == String.class) {
            rv = cursor.getString(0);
        } else if (klass == Short.class || klass == short.class) {
            rv = cursor.getShort(0);
        } else if(klass == Byte.class || klass == byte.class) {
            rv = cursor.getBlob(0)[0];
        } else if (klass == Void.class || klass == void.class) {
            rv = cursor.getBlob(0);
        } else if (klass.isEnum()) {
            rv = Enum.valueOf((Class<? extends Enum>)klass, cursor.getString(0));
        }

        cursor.close();

        return rv;
    }

    @Override
    protected void onPostExecute(final Object count) {
        super.onPostExecute(count);
        if (mListener != null)
            mListener.onResult(count);
    }
}
