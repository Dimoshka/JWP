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
import android.widget.ListView;

import ua.pp.dimoshka.classes.class_news_adapter;

public class news extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private class_news_adapter mAdapter;

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
        mAdapter = new class_news_adapter(
                getActivity(), R.layout.list_items_news_img, null, new String[]{}, new int[]{}, 0, main.database, main.funct);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter("update");
        broadcastManager.registerReceiver(receiver, intentFilter);
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
                getLoaderManager().restartLoader(0, null, this);
            }
        } catch (Exception e) {
            main.funct.send_bug_report(e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(getActivity(), main.database);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    static class MyCursorLoader extends CursorLoader {
        SQLiteDatabase database;

        public MyCursorLoader(Context context, SQLiteDatabase database) {
            super(context);
            this.database = database;
        }

        @Override
        public Cursor loadInBackground() {
            Cursor cursor = main.database.rawQuery("select * from news where news.id_lang='" + main.id_lng + "' order by pubdate desc, news._id asc", null);
            return cursor;
        }

    }

}