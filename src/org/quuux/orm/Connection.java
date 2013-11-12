package org.quuux.orm;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Arrays;


// FIXME thread safety?  i think it's fair to get a new connection per thread

public class Connection {

    private static final String TAG = "org.quuux.orm.Connection";
    private static final boolean LOG_SQL = true;
    private final SQLiteDatabase mDatabase;

    private boolean mInTransaction = false;

    protected Connection(final SQLiteDatabase database) {
        mDatabase = database;
    }

    public boolean isReadable() {
        return mDatabase.isReadOnly();
    }

    public boolean isWritable() {
        return !mDatabase.isReadOnly();
    }

    private void log(final String sql, long time, final String[] args) {
        if (LOG_SQL) {
            Log.d(TAG, String.format("SQL: %d ms :  %s (%s) ", time, sql, args != null ? Arrays.toString(args) : ""));
        }
    }

    public Cursor query(final String sql, final String[] args) {
        final long t1 = System.currentTimeMillis();
        final Cursor rv = mDatabase.rawQuery(sql, args);
        final long t2 = System.currentTimeMillis();
        log(sql, t2 - t1, args);
        return rv;
    }

    public Cursor query(final String sql) {
        return query(sql, null);
    }


    public void exec(final String sql, final String[] args) {
        final long t1 = System.currentTimeMillis();
        mDatabase.execSQL(sql, args);
        final long t2 = System.currentTimeMillis();
        log(sql, t2 - t1, args);
    }

    public void exec(final String sql) {
        final long t1 = System.currentTimeMillis();
        mDatabase.execSQL(sql);
        final long t2 = System.currentTimeMillis();
        log(sql, t2 - t1, null);
    }

    public void commit() {
        if (!mInTransaction)
            throw new IllegalStateException("Commit not in transaction!");

        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        mInTransaction = false;
    }

    public void rollback() {
        if (!mInTransaction)
            throw new IllegalStateException("Commit not in transaction!");

        mDatabase.endTransaction();
        mInTransaction = false;
    }

    public void beginTransaction() {

        if (mInTransaction)
            throw new IllegalStateException("Starting a new transaction while in a transaction!");

        mInTransaction = true;
        mDatabase.beginTransactionNonExclusive();
    }

    public boolean isInTransaction() {
        return mInTransaction;
    }

    public long getLastInsertId(final Class<? extends Entity> entity) {
        final String sql = SchemaBuilder.renderGetLastInsertId(entity);
        final Cursor c = query(sql);
        return c.moveToFirst() ? c.getLong(0) : -1;
    }

    private long getRowsAffected() {
        final String sql = SchemaBuilder.renderRowsAffected();
        final Cursor c = query(sql);
        return c.moveToFirst() ? c.getLong(0) : -1;
    }

    public void replace(final Entity e) {

        final String sql = SchemaBuilder.renderReplace(e);
        final String[] args = SchemaBuilder.gatherArgs(e);
        exec(sql, args);

        if (SchemaBuilder.hasAutoincrement(e) && SchemaBuilder.getAutoincrementValue(e) <= 0) {
            final long lastInsertid = getLastInsertId(e.getClass());
            if (lastInsertid > 0) {
                SchemaBuilder.setAutoincrement(e, lastInsertid);
            }
        }
    }

    public static Connection openReadable(final Database database) {
        return new Connection(database.getReadableDatabase());
    }

    public static Connection openWritable(final Database database) {
        return new Connection(database.getWritableDatabase());
    }

    public boolean delete(final Entity e) {
        final String sql = SchemaBuilder.renderDelete(e);
        final String[] args = {String.valueOf(SchemaBuilder.getPrimaryKeyValue(e))};
        exec(sql, args);

        final long rowsAffected = getRowsAffected();

        final boolean rv = rowsAffected == 1;

        // FIXME wipe PK/autoincrement?

        return rv;
    }

}
