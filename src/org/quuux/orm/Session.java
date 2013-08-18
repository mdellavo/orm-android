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

    public Query query(final Class<? extends Entity> klass) {
        if (isDirty())
            flush();

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

    public void commit() {
        if (isDirty()) {
            mConnection.beginTransaction();
            flush();
            mConnection.commit();
        }
    }

    public void flush() {
        for (final Entity e : mPendingDeletion) {
            mConnection.delete(e);
        }
        mPendingDeletion.clear();

        for (final Entity e : mPendingInsertion) {
             mConnection.replace(e);
        }
        mPendingInsertion.clear();
    }
}
