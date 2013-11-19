package org.quuux.orm;

import java.util.ArrayList;
import java.util.List;

public class Session {

    private static final String TAG = "Session";
    private final Connection mConnection;

    private List<Entity> mPendingInsertion = new ArrayList<Entity>();
    private List<Entity> mPendingDeletion = new ArrayList<Entity>();

    protected  Session(final Connection connection) {
        mConnection = connection;
    }

    public Connection getConnection() {
        return mConnection;
    }

    public Query bind(final Query query) {
        return new Query(this, query);
    }

    public Query query(final Class<? extends Entity> klass) {
        if (isDirty())
            flush(null);

        return new Query(this, klass);
    }

    public void add(final Entity e) {
        mPendingInsertion.add(e);
    }

    public void add(final List<Entity> entities) {
        for (final Entity e : entities)
            add(e);
    }

    public void delete(final Entity e) {
        mPendingDeletion.add(e);
    }

    public boolean isDirty() {
        return mPendingInsertion.size() > 0 || mPendingDeletion.size() > 0;
    }

    public void commit(final FlushListener listener) {
        if (isDirty()) {
            flush(listener);
        } else {
            if (listener != null)
                listener.onFlushed();
        }
    }

    public void commit() {
        commit(null);
    }

    public void flush(final FlushListener listener) {
        new FlushTask(mConnection, listener).execute(mPendingDeletion, mPendingInsertion);
        mPendingDeletion = new ArrayList<Entity>();
        mPendingInsertion = new ArrayList<Entity>();
    }
}
