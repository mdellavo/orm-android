package org.quuux.orm;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
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

    public FlushTask commit(final FlushListener listener) {

        if (isDirty()) {
            return flush(listener);
        } else {
            if (listener != null)
                listener.onFlushed();
        }

        return null;
    }

    public FlushTask commit() {
        return commit(null);
    }

    public FlushTask flush(final FlushListener listener) {

        final FlushListener listenerDelegate = new FlushListener() {

            @Override
            public void onFlushed() {
                if (listener != null)
                    listener.onFlushed();
            }
        };

        final List<Entity> pendingDeletion = new ArrayList<Entity>();
        pendingDeletion.addAll(mPendingDeletion);
        mPendingDeletion.clear();

        final List<Entity> pendingInsertion = new ArrayList<Entity>();
        pendingInsertion.addAll(mPendingInsertion);
        mPendingInsertion.clear();

        final FlushTask t = new FlushTask(mConnection, listenerDelegate);
        execute(t, pendingDeletion, pendingInsertion);
        return t;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public <Param, Progress, Result> void execute(final AsyncTask<Param, Progress, Result> task, Param... args) {

        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                task.executeOnExecutor(mExecutor, args);
            else
                task.execute(args);
        } catch (final Exception e) {
            Log.e(TAG, "failed to execute query", e);
        }

    }
}
