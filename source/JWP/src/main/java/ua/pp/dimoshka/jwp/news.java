package ua.pp.dimoshka.jwp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import ua.pp.dimoshka.classes.class_news_adapter;

public class news extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.OnScrollListener {

    private class_news_adapter mAdapter = null;
    private static main main = null;

    private int currentVisibleItemCount;
    private int currentScrollState;
    private final int load_items = 10;
    private int curent_load_items = 0;
    private boolean isLoading = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("update")) {
                refresh();
            }
        }
    };


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        main = (main) getActivity();
        mAdapter = new class_news_adapter(
                getActivity(), new String[]{}, new int[]{}, main.get_database(), main.get_funct());
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter("update");
        broadcastManager.registerReceiver(receiver, intentFilter);
        getListView().setOnScrollListener(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mAdapter.getItem(position);
        Cursor cursor = ((class_news_adapter) l.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri data = Uri.parse(cursor.getString(cursor.getColumnIndex("link")));
        intent.setDataAndType(data, "text/html");
        startActivity(intent);
    }


    private void refresh() {
        try {
            if (isAdded()) {
                isLoading = false;
                getLoaderManager().restartLoader(0, null, this);
            }
        } catch (Exception e) {
            main.get_funct().send_bug_report(e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(getActivity(), main.get_database(), (load_items + curent_load_items));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        Log.d("Scroll", cursor.getCount() + " - " + curent_load_items);
        if (cursor.getCount() > curent_load_items) {
            isLoading = false;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        currentVisibleItemCount = visibleItemCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        currentScrollState = scrollState;
        isScrollCompleted();
    }

    private void isScrollCompleted() {
        if (currentVisibleItemCount > 0 && currentScrollState == SCROLL_STATE_IDLE) {
            if (!isLoading) {
                isLoading = true;
                try {
                    if (isAdded()) {
                        curent_load_items += load_items;
                        Log.d("Scroll", "load more: " + (curent_load_items + load_items));
                        getLoaderManager().restartLoader(0, null, this);
                    }
                } catch (Exception e) {
                    main.get_funct().send_bug_report(e);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    static class MyCursorLoader extends CursorLoader {
        final SQLiteDatabase database;
        final int limit;

        public MyCursorLoader(Context context, SQLiteDatabase database, int limit) {
            super(context);
            this.database = database;
            this.limit = limit;
        }

        @Override
        public Cursor loadInBackground() {
            @SuppressWarnings("UnnecessaryLocalVariable") Cursor cursor = database.rawQuery("select * from news where news.id_lang='" + main.get_funct().get_id_lng() + "' order by news._id DESC limit 0, " + limit, null);
            return cursor;
        }

    }

}