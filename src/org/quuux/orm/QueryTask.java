package org.quuux.orm;

import android.database.Cursor;
import android.os.AsyncTask;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class QueryTask<T extends Entity> extends AsyncTask<Object, Void, List<T>> {

    private static final String TAG = Log.buildTag(QueryTask.class);

    private final Query mQuery;
    private final Connection mConnection;
    private final QueryListener<T> mListener;

    public QueryTask(final Connection connection, final Query query, final QueryListener<T> listener) {
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
    protected List<T> doInBackground(Object... params) {
        final Cursor cursor = query();

        List<T> rv = null;
        if (cursor.moveToFirst()) {
            rv = new ArrayList<T>();

            do {
                final T e = hydrate(cursor);
                if (e != null)
                    rv.add(e);
            } while(cursor.moveToNext());
        }

        cursor.close();

        return rv;
    }

    @Override
    protected void onPostExecute(List<T> result) {
        if (mListener != null) {
            mListener.onResult(result);
        }
    }

    private T hydrate(final Cursor cursor) {
        T rv = null;
        try {
            rv = (T) mQuery.getEntity().newInstance();

            // FIXME move elsewhere
            final Field[] fields = mQuery.getEntity().getDeclaredFields();
            for (final Field f : fields) {

                if (!SchemaBuilder.isColumn(f))
                    continue;

                final String columnName = SchemaBuilder.getColumnName(f);
                final SchemaBuilder.SqlType sqlType = SchemaBuilder.mapType(f);
                final Class<?> type = f.getType();
                final int colIndex = cursor.getColumnIndex(columnName);

                f.setAccessible(true);

                if (type == Boolean.class || type == boolean.class) {
                    f.setBoolean(rv, cursor.getInt(colIndex) != 0);
                } else if(type == Integer.class || type == int.class) {
                    f.setInt(rv, cursor.getInt(colIndex));
                } else if (type == Long.class || type == long.class) {
                    f.setLong(rv, cursor.getLong(colIndex));
                } else if (type == Float.class || type == float.class) {
                    f.setFloat(rv, cursor.getFloat(colIndex));
                } else if (type == Double.class || type == double.class) {
                    f.setDouble(rv, cursor.getDouble(colIndex));
                } else if (type == Character.class || type == char.class) {
                    f.setChar(rv, cursor.getString(colIndex).charAt(0));
                } else if (type == String.class) {
                    f.set(rv, cursor.getString(colIndex));
                } else if (type == Short.class || type == short.class) {
                    f.setShort(rv, cursor.getShort(colIndex));
                } else if(type == Byte.class || type == byte.class) {
                    f.setByte(rv, cursor.getBlob(colIndex)[0]);
                } else if (type == Void.class || type == void.class) {
                    f.set(rv, cursor.getBlob(colIndex));
                } else if (type.isEnum()) {
                    f.set(rv, Enum.valueOf((Class<? extends Enum>)type, cursor.getString(colIndex)));
                }
            }
        } catch (final InstantiationException e) {
            Log.e(TAG, "error hydrating entity %s", e, mQuery.getEntity().getName());
        } catch (final IllegalAccessException e) {
            Log.e(TAG, "error hydrating entity %s", e, mQuery.getEntity().getName());
        }

        return rv;
    }




}
