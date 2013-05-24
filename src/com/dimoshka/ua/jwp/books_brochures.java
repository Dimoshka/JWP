package com.dimoshka.ua.jwp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockFragment;
import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_rss_books_brochures_adapter;
import com.dimoshka.ua.classes.class_rss_books_brochures_img;

import java.io.File;
import java.util.ArrayList;

@SuppressLint("HandlerLeak")
public class books_brochures extends SherlockFragment {
    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (main.prefs.getBoolean("downloads_img", true)) {
                        Log.e("JWP", "start load image");
                        jwp_rss_img();
                    } else {
                        refresh();
                    }
                    break;
                case 2:
                    Log.e("JWP", "refrashe afte load");
                    refresh();
                    break;
            }
        }
    };

    private ListView list;
    private class_rss_books_brochures_img rss_books_brochures_img;
    private Cursor cursor;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group,
                             Bundle saved) {
        return inflater.inflate(R.layout.list, group, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        rss_books_brochures_img = new class_rss_books_brochures_img(getActivity(), handler,
                main.database);

        try {
            list = (ListView) getActivity().findViewById(R.id.list);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    cursor.moveToPosition(position);
                    main.open_or_download.dialog_show(cursor.getString(cursor.getColumnIndex("_id")));
                }
            });


        } catch (Exception e) {
            main.funct.send_bug_report(getActivity(), e, getClass().getName(),
                    106);
        }

        if (main.prefs.getBoolean("downloads_img", true)) {
            Log.e("JWP", "start load image");
            jwp_rss_img();
        } else {
            refresh();
        }
    }


    public void refresh() {
        try {
            getActivity().stopManagingCursor(cursor);
            cursor = main.database
                    .rawQuery(
                            "select magazine._id as _id, magazine.name as name, magazine.title as title, magazine.img as img, language.code as code_lng, publication.code as code_pub, publication._id as cur_pub, date from magazine left join language on magazine.id_lang=language._id left join publication on magazine.id_pub=publication._id where magazine.id_lang='"
                                    + main.id_lang
                                    + "' and magazine.id_pub='4' order by date desc, magazine.id_pub asc",
                            null);
            getActivity().startManagingCursor(cursor);
            ArrayList<String> files_arr = new ArrayList();
            cursor.moveToFirst();

            for (int i = 0; i < cursor.getCount(); i++) {
                Integer _id = cursor.getInt(cursor.getColumnIndex("_id"));
                Cursor cur = main.database.rawQuery(
                        "select id_type, file, name from files where `id_magazine`='"
                                + _id + "' group by id_type", null);

                getActivity().startManagingCursor(cur);

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

                            Log.d("JWP" + getClass().getName(),
                                    cur.getString(cur.getColumnIndex("name")));

                            if (file.exists()) {
                                file_isn = 1;
                            } else {
                                Log.d("JWP" + getClass().getName(),
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
                getActivity().stopManagingCursor(cur);
                files_arr.add(files);
                cursor.moveToNext();
            }


            cursor.moveToFirst();
            class_rss_books_brochures_adapter scAdapter = new class_rss_books_brochures_adapter(getActivity(),
                    R.layout.list_items_books_brochures, cursor, new String[]{"_id"}, new int[]{R.id.title}, main.database, files_arr);
            list.setAdapter(scAdapter);


        } catch (Exception e) {
            main.funct.send_bug_report(getActivity(), e, getClass().getName(),
                    392);
        }
    }

    public void jwp_rss_img() {
        rss_books_brochures_img.verify_all_img();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().stopManagingCursor(cursor);
        getActivity().stopService(
                new Intent(getActivity(), class_downloads_files.class));

    }
}