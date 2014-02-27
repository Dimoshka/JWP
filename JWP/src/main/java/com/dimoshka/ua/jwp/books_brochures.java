package com.dimoshka.ua.jwp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dimoshka.ua.classes.class_books_brochures_adapter;
import com.dimoshka.ua.classes.class_downloads_files;

import java.io.File;
import java.util.ArrayList;

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
            main.funct.send_bug_report(getActivity(), e, "books_broshures",
                    106);
        }

        refresh();

        return view;
    }

    public void refresh() {
        try {
            cursor = main.database
                    .rawQuery(
                            "select magazine._id as _id, magazine.name as name, magazine.title as title, magazine.img as img, language.code as code_lng, publication.code as code_pub, publication._id as cur_pub, date from magazine left join language on magazine.id_lang=language._id left join publication on magazine.id_pub=publication._id where magazine.id_lang='"
                                    + main.id_lang
                                    + "' and magazine.id_pub='4' order by magazine.name asc",
                            null);
            ArrayList<String> files_arr = new ArrayList<String>();
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                Integer _id = cursor.getInt(cursor.getColumnIndex("_id"));
                Cursor cur = main.database.rawQuery(
                        "select id_type, file, name from files where `id_magazine`='"
                                + _id + "' group by id_type", null);

                String files = "";
                if (cur.getCount() > 0) {
                    cur.moveToFirst();
                    for (int a = 0; a < cur.getCount(); a++) {
                        if (files.length() > 0)
                            files = files + ",";
                        int file_isn = 0;

                        if (cur.getInt(cur.getColumnIndex("file")) == 1) {
                            File file = new File(
                                    main.funct.get_dir_app(getActivity())
                                            + "/downloads/"
                                            + cur.getString(cur
                                            .getColumnIndex("name")));

                            if (file.exists()) {
                                file_isn = 1;
                            } else {
                                Log.e("JWP" + "books_broshures",
                                        "Update to 0 - "
                                                + cur.getString(cur
                                                .getColumnIndex("name")));
                                ContentValues initialValues = new ContentValues();
                                initialValues.put("file", "0");
                                main.database.update("files", initialValues,
                                        "name=?",
                                        new String[]{cur.getString(cur
                                                .getColumnIndex("name"))});
                            }
                        }

                        files = files
                                + cur.getString(cur.getColumnIndex("id_type"))
                                + "=" + file_isn;
                        cur.moveToNext();

                    }
                }
                files_arr.add(files);
                cursor.moveToNext();
            }

            cursor.moveToFirst();
            scAdapter = new class_books_brochures_adapter(
                    getActivity(), R.layout.list_items_books_brochures, cursor,
                    new String[]{"_id"}, new int[]{R.id.title},
                    main.database, files_arr);
            list.setAdapter(scAdapter);
        } catch (Exception e) {
            main.funct.send_bug_report(getActivity(), e, "books_broshures",
                    392);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().stopService(
                new Intent(getActivity(), class_downloads_files.class));

    }
}