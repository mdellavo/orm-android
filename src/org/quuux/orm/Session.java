package org.quuux.orm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T create(final Class<? extends Entity> klass, Object... args) {


        try {
            final T obj =  (T) klass.getConstructor().newInstance();

            return (T) Proxy.newProxyInstance(klass.getClassLoader(),  klass.getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

                    final Relation relation = method.getAnnotation(Relation.class);

                    if (relation != null ) {
                        Log.d(TAG, "HOMG A RELATION!");
                    }

                    return method.invoke(obj, args);
                }
            });

        } catch (InstantiationException e) {
            Log.e(TAG, "Could not create instance of %s: %s", klass, e);
            return null;
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Could not create instance of %s: %s", klass, e);
            return null;
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Could not create instance of %s: %s", klass, e);
            return null;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Could not create instance of %s: %s", klass, e);
            return null;
        }
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
