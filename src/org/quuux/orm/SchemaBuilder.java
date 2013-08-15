package org.quuux.orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// this is a mess...

// FIXME move sql generator into SQLite specific generator class
// FIXME move introspection utils into introspection class

public class SchemaBuilder {

    private static final String EMPTY = "";
    private static final String TAG = "org.quuux.orm.SchemaBuilder";


    enum SqlType {
        INTEGER,
        TEXT,
        REAL,
        NUMERIC,
        NONE
    };

    private static Map<Class<?>, SqlType> typeMap = new HashMap<Class<?>, SqlType>();

    static {
        typeMap.put(Boolean.class, SqlType.NUMERIC);
        typeMap.put(boolean.class, SqlType.NUMERIC);
        typeMap.put(Integer.class, SqlType.INTEGER);
        typeMap.put(int.class, SqlType.INTEGER);
        typeMap.put(Long.class, SqlType.INTEGER);
        typeMap.put(long.class, SqlType.INTEGER);
        typeMap.put(Float.class, SqlType.REAL);
        typeMap.put(float.class, SqlType.REAL);
        typeMap.put(Double.class, SqlType.REAL);
        typeMap.put(double.class, SqlType.REAL);
        typeMap.put(Character.class, SqlType.TEXT);
        typeMap.put(char.class, SqlType.TEXT);
        typeMap.put(String.class, SqlType.TEXT);
        typeMap.put(Byte.class, SqlType.NONE);
        typeMap.put(byte.class, SqlType.NONE);
        typeMap.put(Void.class, SqlType.NONE);
        typeMap.put(void.class, SqlType.NONE);
        typeMap.put(Short.class, SqlType.INTEGER);
        typeMap.put(short.class, SqlType.INTEGER);
    }

    public static Table getTable(final Class<? extends Entity> entity) {
        if (!entity.isAnnotationPresent(Table.class))
            throw new IllegalArgumentException(Table.class.getSimpleName() + " does not have a Table annotation");

        return (Table) entity.getAnnotation(Table.class);
    }

    public static Table getTable(final Entity e) {
        return getTable(e.getClass());
    }

    public static Column getColumn(final Field field) {
        return field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class) : null;
    }

    public static String renderCreateTable(Class<? extends Entity> entity) {
        final StringBuilder sb = new StringBuilder();

        final Table table = getTable(entity);
        final Field[] fields = entity.getDeclaredFields();

        append(sb, "CREATE TABLE IF NOT EXISTS %s (", table.name());
        sb.append("\n");

        for (int i = 0; i<fields.length; i++) {
            final Field field = fields[i];
            sb.append("    ");
            sb.append(renderColumn(field));

            if (i<fields.length-1)
                sb.append(",");

            sb.append("\n");
        }

        sb.append(");");

        return sb.toString();
    }

    private static String renderColumn(final Field field) {
        final Column column = getColumn(field);
        final SqlType type = mapType(field);

        final StringBuilder sb = new StringBuilder();

        sb.append(getColumnName(field));
        sb.append(" ");
        sb.append(type.name());

        if (column.primaryKey()) {
            sb.append(" PRIMARY KEY");
            if (type == SqlType.INTEGER)
                sb.append(" AUTOINCREMENT");
        }

        if (!column.nullable())
            sb.append(" NOT NULL");

        if (column.unique())
            sb.append(" UNIQUE");

        if (notEmpty(column.check()))
            append(sb, " CHECK (%s)", column.check());

        if (notEmpty(column.defaultValue()))
            append(sb, " DEFAULT (%s)", column.defaultValue());

        return sb.toString();
    }

    public static String getColumnName(final Field field, final Column column) {
        return notEmpty(column.name()) ? column.name() : field.getName();
    }

    public static String getColumnName(final Field field) {
        final Column column = getColumn(field);
        return getColumnName(field, column);
    }

    private static boolean notEmpty(final String s) {
        return s != null && !EMPTY.equals(s);
    }

    private static void append(final StringBuilder sb, final String fmt, final Object... args) {
        sb.append(String.format(fmt, args));
    }

    public static SqlType mapType(final Field field) {
        return typeMap.get(field.getType());
    }

    public static String renderDropTable(Class<? extends Entity> entity) {
        final Table table = getTable(entity);
        return String.format("DROP TABLE IF EXISTS %s;\n", table.name());
    }

    private static boolean isAutoincrement(final Field f) {
        final Column column = getColumn(f);
        final SqlType type = mapType(f);

        return column.primaryKey() && type == SqlType.INTEGER;
    }

    private static Field getAutoincrement(final Class<? extends Entity> entity) {
        final Field[] fields = entity.getDeclaredFields();

        for(final Field f: fields) {
            if (isAutoincrement(f))
                return f;
        }

        return null;
    }

    private static Field getAutoincrement(final Entity e) {
        return getAutoincrement(e.getClass());
    }

    public static boolean hasAutoincrement(final Class<? extends Entity> entity) {
        return getAutoincrement(entity) != null;
    }

    public static boolean hasAutoincrement(final Entity e) {
        return hasAutoincrement(e.getClass());
    }

    public static void setAutoincrement(final Entity e, final long id) {
        final Field f = getAutoincrement(e);
        try {
            f.setAccessible(true);
            f.set(e, id);
        } catch (IllegalAccessException e1) {
            Log.e(TAG, "Could not set id " + id + " on entity " + e + ": " + e.toString());
        }
    }

    public static Object getValue(final Entity entity, final Field field) {

        Object rv = null;
        try {
            field.setAccessible(true);
            rv = field.get(entity);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Could not get value of field " + field + ": " + e);
        }

        return rv;
    }

    public static long getAutoincrementValue(final Entity entity, final Field field) {

        long rv = -1;
        try {
            field.setAccessible(true);
            rv = field.getLong(entity);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Could not get autoincrement value for " + e + ": " + e);
        }

        return rv;
    }

    public static long getAutoincrementValue(final Entity entity) {
        final Field f = getAutoincrement(entity);
        return getAutoincrementValue(entity, f);
    }

    public static String renderReplace(final Entity e) {

        Log.d(TAG, "e = %s", e);

        final Table table = getTable(e.getClass());
        final Field[] fields = e.getClass().getDeclaredFields();

        final StringBuilder sb = new StringBuilder();

        sb.append("INSERT OR REPLACE INTO ");
        sb.append(table.name());
        sb.append(" VALUES (");

        for (int i=0; i<fields.length; i++) {

            sb.append("?");
            if (i<fields.length-1)
                sb.append(", ");
        }

        sb.append(")");

        return sb.toString();
    }

    public static String[] gatherArgs(final Entity e) {
        final List<String> args = new ArrayList<String>();

        final Field[] fields = e.getClass().getDeclaredFields();

        for (final Field f : fields) {

            f.setAccessible(true);

                if (isAutoincrement(f) && getAutoincrementValue(e, f) <= 0){
                    args.add(null);
                } else {
                    args.add(getValue(e, f).toString());
                }
        }

        return args.toArray(new String[args.size()]);
    }

    public static String renderGetLastInsertId(final Class<? extends Entity> entity) {
        return String.format("SELECT ROWID from %s ORDER BY ROWID DESC LIMIT 1", getTable(entity).name());
    }

    public static String renderRowsAffected() {
        return "SELECT changes();";
    }

    public static String renderDelete(final Entity e) {
        final Table table = getTable(e);
        final Field field = getPrimaryKey(e);
        final String columnName = getColumnName(field);
        return String.format("DELETE FROM %s WHERE %s = ?", table.name(), columnName);
    }

    private static Field getPrimaryKey(final Class<? extends Entity> entity) {
        final Field[] fields = entity.getDeclaredFields();
        for (final Field f : fields) {
            final Column column = getColumn(f);
            if (column.primaryKey())
                return f;
        }

        return null;
    }

    private static Field getPrimaryKey(final Entity e) {
        return getPrimaryKey(e.getClass());
    }

    public static Object getPrimaryKeyValue(final Entity e) {
        return getValue(e, getPrimaryKey(e));
    }

    public static String renderQuery(final Query query) {
        final Field[] fields = query.getEntity().getDeclaredFields();
        final Table table = getTable(query.getEntity());

        final StringBuilder sb = new StringBuilder();

        sb.append("SELECT ");

        for(int i=0; i<fields.length; i++) {
            sb.append(getColumnName(fields[i]));
            if (i<fields.length-1)
                sb.append(", ");
        }

        sb.append(" FROM ");
        sb.append(table.name());

        if (notEmpty(query.getSelection())) {
            sb.append(" WHERE ");
            sb.append(query.getSelection());
        }

        if (notEmpty(query.getOrderBy())) {
            sb.append(" ORDER BY ");
            sb.append(query.getOrderBy());
        }

        if (query.getLimit() > 0) {
            sb.append(" LIMIT ");
            sb.append(query.getLimit());
        }

        if (query.getOffset() > 0) {
            sb.append(" OFFSET ");
            sb.append(query.getOffset());
        }

        return sb.toString();
    }

    public static String renderGetClause(final Class<? extends Entity> mEntity) {
        final Field primaryKey = getPrimaryKey(mEntity);
        final StringBuilder sb = new StringBuilder();
        sb.append(getColumnName(primaryKey));
        sb.append(" = ?");
        return sb.toString();
    }


}
