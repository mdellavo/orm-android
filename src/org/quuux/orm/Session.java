package org.quuux.orm;

import android.os.AsyncTask;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Session {

    private static final String TAG = "Session";
    private final Connection mConnection;

    private List<Entity> mPendingInsertion = new ArrayList<Entity>();
    private List<Entity> mPendingDeletion = new ArrayList<Entity>();

    private Executor mExecutor;

    protected  Session(final Connection connection) {
        mConnection = connection;
        mExecutor = Executors.newSingleThreadExecutor();
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
        final AsyncTask task = new FlushTask(mConnection, listener).executeOnExecutor(mExecutor, mPendingDeletion, mPendingInsertion);
        mPendingDeletion = new ArrayList<Entity>();
        mPendingInsertion = new ArrayList<Entity>();
    }

    public Executor getExecutor() {
        return mExecutor;
    }

    public void execute(final AsyncTask task, Object... args) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            task.executeOnExecutor(mExecutor, args);
        else
            task.execute(args);
    }

}
