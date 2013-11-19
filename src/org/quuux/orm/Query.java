package org.quuux.orm;

import android.content.Context;

import java.io.Serializable;

public class Query implements Clause, Serializable {

    protected transient final Session mSession;
    protected final Class<? extends Entity> mEntity;

    protected Clause mProjection;
    protected Clause mSelection;
    protected Object[] mSelectionArgs;
    protected String mOrderBy;
    protected int mLimit, mOffset;
   
    public Query(final Session session, Class<? extends Entity> klass) {
        mSession = session;
        mEntity = klass;
    }

    public Query(final Query other) {
        this(other.mSession, other.mEntity);
        copyOf(other);
    }

    public Query(final Session session, final Query other) {
        this(session, other.getEntity());
        copyOf(other);
    }

    public Class<? extends Entity> getEntity() {
        return mEntity;
    }

    private void copyOf(final Query other) {
        mProjection = other.mProjection;
        mSelection = other.mSelection;
        mSelectionArgs = other.mSelectionArgs;
        mOrderBy = other.mOrderBy;
        mLimit = other.mLimit;
        mOffset = other.mOffset;
    }

    // FIXME this needs to take a fetch listener and fetch
    public Query get(final Object pk, final FetchListener<? extends Entity> listener) {
        final Query rv = new Query(this);
        rv.mSelection = new Literal(SchemaBuilder.renderGetClause(mEntity));
        rv.mSelectionArgs = new Object[] { pk };

        first(listener);

        return rv;
    }

    public boolean isBound() {
        return mSession != null;
    }

    public Query bind(final Session session) {
        return new Query(session, this);
    }

    public  Clause getProjection() {
        return mProjection;
    }

    public void setProjection(final Clause projection) {
        mProjection = projection;
    }

    public void setProjection(final String projection) {
        setProjection(new Literal(projection));
    }

    public void setSelection(final Clause selection) {
        mSelection = selection;
    }

    public void setSelection(final String selection) {
        setSelection(new Literal(selection));
    }

    public Clause getSelection() {
        return mSelection;
    }

    public Object[] getSelectionArgs() {
        return mSelectionArgs;
    }

    public Query project(final String projection) {
        final Query rv = new Query(this);
        rv.mProjection = new Literal(projection);
        return rv;
    }

    public Query filter(final String selection, final Object... args) {
        final Query rv = new Query(this);
        rv.mSelection = new Literal(selection); // FIXME this needs to be AND'd
        rv.mSelectionArgs = args;
        return rv;
    }

    public Query orderBy(final String orderBy) {
        final Query rv = new Query(this);
        rv.mOrderBy = orderBy;
        return rv;
    }

    public String getOrderBy() {
        return mOrderBy;
    }

    public Query limit(final int limit) {
        final Query rv = new Query(this);
        rv.mLimit = limit;
        return rv;
    }

    public int getLimit() {
        return mLimit;
    }

    public Query limit(final int limit, final int offset) {
        final Query rv = new Query(this);
        rv.mLimit = limit;
        rv.mOffset = offset;
        return rv;
    }

    public Query offset(final int offset) {
        final Query rv = new Query(this);
        rv.mOffset = offset;
        return rv;
    }

    public Query count() {
        final Query rv = new Query(this);
        rv.mProjection = Func.COUNT;
        return rv;
    }

    public void count(ScalarListener<Long> listener) {
        count().scalar(Long.class, listener);
    }

    public int getOffset() {
        return mOffset;
    }

    public void first(final FetchListener<? extends Entity> listener) {
        new FetchTask(mSession.getConnection(), this, listener).execute();
    }

    public void all(final QueryListener<? extends Entity> listener) {
        new QueryTask(mSession.getConnection(), this, listener).execute();
    }

    public <T> void scalar(Class<T> klass, final ScalarListener<T> listener) {
        new ScalarTask(mSession.getConnection(), this, listener).execute(klass);
    }

    public String toSql() {
        return SchemaBuilder.renderQuery(this);
    }

}
