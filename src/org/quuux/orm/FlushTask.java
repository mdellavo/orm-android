package org.quuux.orm;

import android.os.AsyncTask;

import java.util.List;

public class FlushTask extends AsyncTask<List<Entity>, Void, Void> {

    private static final String TAG = Log.buildTag(FlushTask.class);

    private final Connection mConnection;
    private final FlushListener mListener;

    public FlushTask(final Connection connection, final FlushListener listener) {
        mConnection = connection;
        mListener = listener;
    }

    @Override
    protected Void doInBackground(final List<Entity>... params) {

        final List<Entity> pendingDeletion = params[0];
        final List<Entity> pendingInsertion = params[1];

        mConnection.beginTransaction();

        for (final Entity e : pendingDeletion) {
            mConnection.delete(e);
        }

        for (final Entity e : pendingInsertion) {
            mConnection.replace(e);
        }

        mConnection.commit();

        return null;
    }

    @Override
    protected void onPostExecute(final Void aVoid) {
        super.onPostExecute(aVoid);
        if (mListener != null)
            mListener.onFlushed();
    }
}