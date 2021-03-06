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

    private boolean mLoading = false;
    private long mTotal;

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

    public int getPageSize() {
        return PAGE_SIZE;
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

    public void loadPage() {
        mLoading = true;

        mQuery.count(new ScalarListener<Long>() {
            @Override
            public void onResult(final Long count) {
                mTotal = count;

                if (hasMore()) {
                    final int pageSize = getPageSize();
                    final int offset = mItems.size();
                    mQuery.limit(pageSize, offset).all(QueryAdapter.this);
                }
            }
        });

    }

    @Override
    public void onResult(final List<T> result) {

        if (result != null) {

            if (result.size() > 0) {

                for (final T item : result) {
                    if (!mItems.contains(item)) {
                        mItems.add(item);
                    }
                }

                notifyDataSetChanged();
            }
        }

        onLoadComplete();

        mLoading = false;
    }

    public long getTotal() {
        return mTotal;
    }

    public boolean hasMore() {
        return mTotal > mItems.size();
    }

    protected void onLoadComplete() {
    }


    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
        if (hasMore() && !mLoading && firstVisibleItem + visibleItemCount > totalItemCount - LOOKAHEAD) {
            loadPage();
        }
    }

}
