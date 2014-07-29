package ua.pp.dimoshka.jwp;

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
import android.view.View;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import ua.pp.dimoshka.classes.class_books_brochures_adapter;

public class books_brochures extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    class_books_brochures_adapter mAdapter = null;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("update")) {
                refresh();
            }
        }
    };

    public books_brochures() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new class_books_brochures_adapter(
                getActivity(), R.layout.list_items_books_brochures, null,
                new String[]{"_id"}, new int[]{R.id.title}, 0,
                main.database, main.funct);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter("update");
        broadcastManager.registerReceiver(receiver, intentFilter);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        main.open_or_download.dialog_show(id);
    }

    public void refresh() {
        try {
            if (isAdded()) {
                //getLoaderManager().restartLoader(0, null, this);


                Document doc = Jsoup.connect("http://example.com/").get();
                String title = doc.title();


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
            @SuppressWarnings("UnnecessaryLocalVariable") Cursor cursor = main.database
                    .rawQuery(
                            "select magazine._id as _id, magazine.name as name, magazine.title as title, magazine.img as img, " +
                                    "language.code as code_lng, " +
                                    "publication.code as code_pub, publication._id as cur_pub, date, " +
                                    "files.id_type as id_type_files, files.file as file_files " +
                                    "from magazine " +
                                    "left join language on magazine.id_lang=language._id " +
                                    "left join publication on magazine.id_pub=publication._id " +
                                    "left join (select id_magazine, GROUP_CONCAT(id_type) as id_type, GROUP_CONCAT(file) as file from files group by id_magazine) as files on magazine._id=files.id_magazine " +
                                    "where magazine.id_lang='" + main.id_lng + "' and magazine.id_pub='4' order by magazine.name asc",
                            null
                    );
            return cursor;
        }

    }
}