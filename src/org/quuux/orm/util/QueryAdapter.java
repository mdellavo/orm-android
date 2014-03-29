package org.quuux.orm.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import org.quuux.orm.*;

import java.util.LinkedList;
import java.util.List;

public abstract class QueryAdapter<T extends Entity>
        extends BaseAdapter
        implements QueryListener<T>,
                   AbsListView.OnScrollListener {
    
    private static final String TAG = Log.buildTag(QueryAdapter.class);

    private static final int PAGE_SIZE = 20;
    private static final int LOOKAHEAD = 10;

    final Context mContext;
    final Query mQuery;
    final List<T> mItems = new LinkedList<T>();

    private boolean mHasMore = true;
    private boolean mLoading = false;

    public QueryAdapter(final Context context,final Query query) {
        mContext = context;
        mQuery = query;
        loadPage();
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public T getItem(final int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    public boolean add(final T item) {
        return mItems.add(item);
    }

    public void add(final int position, final T item) {
         mItems.add(position, item);
    }

    public boolean remove(final T item) {
        return mItems.remove(item);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final T item = (T)getItem(position);
        final View rv  = convertView == null ? newView(mContext, item, parent) : convertView;
        bindView(mContext, item, rv, parent);
        return rv;
    }

    public int getPositionForItem(final T obj) {
        int i = 0;

        for(T item : mItems) {
            if (obj == item)
                return i;

            i++;
        }

        return -1;
    }

    protected abstract View newView(final Context context, final T item, final ViewGroup parent);

    protected abstract void bindView(final Context context, final T item, final View view, final ViewGroup parent);

    protected void loadPage() {
        mLoading = true;
        mQuery.limit(PAGE_SIZE, mItems.size()).all(this);
        Log.d(TAG , "loading page @ offset = %d", mItems.size());
    }

    @Override
    public void onResult(final List<T> result) {

        if (result != null) {

            if (result.size() > 0)
                mItems.addAll(result);

            mHasMore = result.size() == PAGE_SIZE;
            if (!mHasMore)
                onLoadComplete();

            Log.d(TAG, "loaded %d items (total = %d, hasMore = %s)", result.size(), mItems.size(), mHasMore);
            notifyDataSetChanged();
        }

        mLoading = false;
    }

    protected void onLoadComplete() {
    }

    public void continueLoading() {
        Log.d(TAG, "continue loading...");
        mHasMore = true;
        loadPage();
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        if (mHasMore && !mLoading && firstVisibleItem + visibleItemCount > totalItemCount - LOOKAHEAD) {
            loadPage();
        }
    }

}
