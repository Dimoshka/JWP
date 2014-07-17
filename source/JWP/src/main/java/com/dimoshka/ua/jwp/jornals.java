package com.dimoshka.ua.jwp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.dimoshka.ua.classes.class_jornals_adapter;

public class jornals extends ListFragment {

    private Cursor cursor;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refresh();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        cursor.moveToPosition(position);
        main.open_or_download.dialog_show(cursor.getString(cursor.getColumnIndex("_id")));
    }

    public void refresh() {
        try {
            cursor = main.database
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
                                    "where magazine.id_lang='" + main.id_lang + "' and magazine.id_pub BETWEEN '1' and '3' order by date desc, magazine.id_pub asc;",
                            null
                    );

            class_jornals_adapter adapter = new class_jornals_adapter(
                    getActivity(), R.layout.list_items_jornals, cursor, new String[]{}, new int[]{}, 0, main.database, main.funct);
            setListAdapter(adapter);

        } catch (Exception e) {
            main.funct.send_bug_report(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}