package org.quuux.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// FIXME move writable database to connection? should probably break it so semantics of when when we have a writable database is clear
// Session deals with writable db

public class Database extends SQLiteOpenHelper {

    private static final String TAG = "org.quuux.orm.Database";

    private static Database instance;
    private static List<Class<? extends Entity>> sEntities = new ArrayList<Class<? extends Entity>>();

    protected Database(final Context context, final String name, final int version) {
        super(context, name, null, version);
    }

    public static void attach(final Class<? extends Entity> klass) {
        sEntities.add(klass);
    }

    public Session createSession() {
        return new Session(Connection.openWritable(this));
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        Log.d(TAG, "creating database");
        initializeDatabase(db);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.d(TAG, "upgrading from " + oldVersion + " -> " + newVersion);
        initializeDatabase(db);
    }

    @Override
    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        Log.d(TAG, "downgrading from " + oldVersion + " -> " + newVersion);
        initializeDatabase(db);
    }

    private void initializeDatabase(final SQLiteDatabase db) {
        Log.d(TAG, "initializing database...");

        final Connection conn = Connection.openWritable(this);

        conn.beginTransaction();
        dropAll(conn, db, sEntities);
        createAll(conn, db, sEntities);
        conn.commit();
    }

    private void createAll(final Connection conn, final SQLiteDatabase db, final List<Class<? extends Entity>> entities) {
        for (Class<? extends Entity> e : entities) {
            Log.d(TAG, "creating table " + SchemaBuilder.getTable(e).name());
            createTable(conn, db, e);
        }
    }

    private void createTable(final Connection conn, final SQLiteDatabase db, Class<? extends Entity> entity) {
        final String sql = SchemaBuilder.renderCreateTable(entity);
        conn.exec(sql);
    }

    private void dropAll(final Connection conn, final SQLiteDatabase db, List<Class<? extends Entity>> entities) {
        for (Class<? extends Entity> e : entities) {
            Log.d(TAG, "dropping table " + SchemaBuilder.getTable(e).name());
            dropTable(conn, db, e);
        }
    }

    private void dropTable(final Connection conn, final SQLiteDatabase db, Class<? extends Entity> entity) {
        final String sql = SchemaBuilder.renderDropTable(entity);
        conn.exec(sql);
    }


    public static Database getInstance(final Context context, final String name, final int version) {
        if (instance == null) {
            instance = new Database(context, name, version);
            instance.getWritableDatabase();
        }

        return instance;
    }


    public static void release() {
        instance = null;
    }

}
