package org.quuux.orm;

import java.lang.reflect.Field;

public class RelationBuilder {

    private static final String TAG = "RelationBuilder";

    private Class<? extends Entity> mRemote;
    private Entity mLocal;

    public RelationBuilder(final Entity local) {
        mLocal = local;
    }

    public static RelationBuilder forEntity(final Entity local) {
        return new RelationBuilder(local);
    }

    public RelationBuilder remote(final Class<? extends  Entity> remote) {
        mRemote = remote;
        return this;
    }

    public Query build(final Session session) {
        final Query query = new Query(session, mRemote);

        boolean reversed = false;
        Field foreignKey = SchemaBuilder.findForeignKey(mRemote, mLocal.getClass());
        if (foreignKey == null) {
            foreignKey = SchemaBuilder.findForeignKey(mLocal.getClass(), mRemote);
            reversed = foreignKey != null;
        }

        if (foreignKey == null)
            throw new IllegalArgumentException(String.format("Could not find foreign key column for %s and %s", mLocal.getClass(), mRemote));

        final Field primaryKey = SchemaBuilder.getPrimaryKey(reversed ? mLocal.getClass() : mRemote);
        final String columnName =  SchemaBuilder.getColumnName(reversed ? SchemaBuilder.getPrimaryKey(mRemote) : foreignKey);
        final String selection = String.format("%s = ?", columnName);
        return query.filter(selection, reversed ? SchemaBuilder.getValue(mLocal, foreignKey) : SchemaBuilder.getPrimaryKeyValue(mLocal));
    }

    public <T extends Entity> void query(final Session session, final QueryListener<? extends Entity> listener) {
        final Query q = build(session);
        q.all(listener);
    }

    public <T extends Entity> void get(final Session session, final FetchListener<? extends Entity> listener) {
        final Query q = build(session);
        q.first(listener);
    }

}
