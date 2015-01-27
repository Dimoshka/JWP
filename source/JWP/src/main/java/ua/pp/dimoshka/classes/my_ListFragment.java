package ua.pp.dimoshka.classes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.AbsListView;

import ua.pp.dimoshka.jwp.main;

/**
 * Created by designers on 13.08.2014.
 */
public class my_ListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.OnScrollListener {

    public static ua.pp.dimoshka.jwp.main main = null;
    private final int load_items = 10;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("update")) {
                refresh();
            }
        }
    };
    public SimpleCursorAdapter mAdapter = null;
    public String sqlite_rawQuery = "";
    private int currentVisibleItemCount = 0;
    private int currentScrollState = 10;
    private int curent_load_items = 0;
    private boolean isLoading = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        main = (main) getActivity();

        setadapter_list();
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter("update");
        broadcastManager.registerReceiver(receiver, intentFilter);
        getListView().setOnScrollListener(this);
    }

    public void setadapter_list() {

    }


    public void refresh() {
        try {
            //if (isAdded()) {
            isLoading = false;
            getLoaderManager().restartLoader(0, null, this);
            //}
        } catch (Exception e) {
            main.get_funct().send_bug_report(e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(getActivity(), main.get_database(), sqlite_rawQuery, (load_items + curent_load_items));
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

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
        super.onDestroy();
    }

    static class MyCursorLoader extends CursorLoader {
        final SQLiteDatabase database;
        final int limit;
        final String sqlite_rawQuery;
        final Context context;

        public MyCursorLoader(Context context, SQLiteDatabase database, String sqlite_rawQuery, int limit) {
            super(context);
            this.database = database;
            this.limit = limit;
            this.sqlite_rawQuery = sqlite_rawQuery;
            this.context = context;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = null;
            if (sqlite_rawQuery != null && sqlite_rawQuery.length() > 0) {
                cursor = database.rawQuery(sqlite_rawQuery.replace(";", "") + " limit 0, " + limit + ";", null);
            }
            return cursor;
        }
    }
}
