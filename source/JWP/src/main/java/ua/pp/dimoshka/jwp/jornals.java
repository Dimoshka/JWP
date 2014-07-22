package ua.pp.dimoshka.jwp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import ua.pp.dimoshka.classes.class_jornals_adapter;

public class jornals extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private class_jornals_adapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new class_jornals_adapter(
                getActivity(), R.layout.list_items_jornals, null, new String[]{}, new int[]{}, 0, main.database, main.funct);
        setListAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        main.open_or_download.dialog_show(id);
    }

    public void refresh() {
        try {
            getLoaderManager().restartLoader(0, null, this);
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
            Cursor cursor = main.database
                    .rawQuery(
                            "select " +
                                    "magazine._id as _id, magazine.name as name, magazine.img as img, " +
                                    "language.code as code_lng, " +
                                    "publication.code as code_pub, publication._id as cur_pub, date, " +
                                    "files.id_type as id_type_files, files.file as file_files " +
                                    "from magazine " +
                                    "left join language on magazine.id_lang=language._id " +
                                    "left join publication on magazine.id_pub=publication._id " +
                                    "left join (select id_magazine, GROUP_CONCAT(id_type) as id_type, GROUP_CONCAT(file) as file from files group by id_magazine) as files on magazine._id=files.id_magazine " +
                                    "where magazine.id_lang='" + main.id_lng + "' and magazine.id_pub BETWEEN '1' and '3' order by date desc, magazine.id_pub asc;",
                            null
                    );
            return cursor;
        }

    }
}