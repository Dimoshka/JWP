package com.dimoshka.ua.jwp;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dimoshka.ua.classes.class_books_brochures_adapter;

public class books_brochures extends Fragment {
    private ListView list;
    private Cursor cursor;
    class_books_brochures_adapter scAdapter;
    View view = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group,
                             Bundle saved) {
        view = inflater.inflate(R.layout.list, group, false);
        try {
            list = (ListView) view.findViewById(R.id.list);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    cursor.moveToPosition(position);
                    main.open_or_download.dialog_show(cursor.getString(cursor
                            .getColumnIndex("_id")));
                }
            });
        } catch (Exception e) {
            main.funct.send_bug_report(e);
        }

        refresh();

        return view;
    }

    public void refresh() {
        try {
             cursor = main.database
                    .rawQuery(
                            "select magazine._id as _id, magazine.name as name, magazine.title as title, magazine.img as img, " +
                                    "language.code as code_lng, " +
                                    "publication.code as code_pub, publication._id as cur_pub, date, " +
                                    "files.id_type as id_type_files, files.file as file_files " +
                                    "from magazine " +
                                    "left join language on magazine.id_lang=language._id " +
                                    "left join publication on magazine.id_pub=publication._id " +
                                    "left join (select id_magazine, GROUP_CONCAT(id_type) as id_type, GROUP_CONCAT(file) as file from files group by id_magazine) as files on magazine._id=files.id_magazine " +
                                    "where magazine.id_lang='" + main.id_lang + "' and magazine.id_pub='4' order by magazine.name asc",
                            null
                    );

            cursor.moveToFirst();
            scAdapter = new class_books_brochures_adapter(
                    getActivity(), R.layout.list_items_books_brochures, cursor,
                    new String[]{"_id"}, new int[]{R.id.title}, 0,
                    main.database, main.funct);
            list.setAdapter(scAdapter);
        } catch (Exception e) {
            main.funct.send_bug_report(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}