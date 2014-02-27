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
import android.widget.ExpandableListView;

import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_rss_jornals_adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class jornals extends Fragment {

    private ExpandableListView list;
    private ArrayList<Map<String, String>> groupData;
    private ArrayList<Map<String, String>> childDataItem;
    private ArrayList<ArrayList<Map<String, String>>> childData;
    private Map<String, String> m;

    int yer = 0;
    int mon = 0;

    private Cursor cursor;
    View view = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group,
                             Bundle saved) {
        view = inflater.inflate(R.layout.expandable_list, group, false);
        try {
            list = (ExpandableListView) view.findViewById(R.id.list);
            list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView arg0, View arg1,
                                            int arg2, int arg3, long arg4) {
                    Map<String, String> hash = new HashMap<String, String>();
                    hash = childData.get(arg2).get(arg3);
                    main.open_or_download.dialog_show(hash.get("_id"));
                    return false;
                }
            });
        } catch (Exception e) {
            main.funct.send_bug_report(getActivity(), e, "jornals",
                    106);
        }
        refresh();
        return view;
    }


    public void refresh() {
        try {
            cursor = main.database
                    .rawQuery(
                            "select magazine._id as _id, magazine.name as name, magazine.img as img, language.code as code_lng, publication.code as code_pub, publication._id as cur_pub, date from magazine left join language on magazine.id_lang=language._id left join publication on magazine.id_pub=publication._id where magazine.id_lang='"
                                    + main.id_lang
                                    + "' and magazine.id_pub BETWEEN '1' and '3' order by date desc, magazine.id_pub asc",
                            null);

            groupData = new ArrayList<Map<String, String>>();
            childData = new ArrayList<ArrayList<Map<String, String>>>();
            childDataItem = new ArrayList<Map<String, String>>();
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {

                String name = cursor.getString(cursor.getColumnIndex("name"));
                String code_lng = cursor.getString(cursor
                        .getColumnIndex("code_lng"));
                String code_pub = cursor.getString(cursor
                        .getColumnIndex("code_pub"));
                Integer img = cursor.getInt(cursor.getColumnIndex("img"));
                Integer _id = cursor.getInt(cursor.getColumnIndex("_id"));
                Date date = main.funct.get_jwp_jornals_rss_date(name, code_pub,
                        code_lng);

                Log.e("JWP", cursor.getString(cursor
                        .getColumnIndex("date")));

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

                            //Log.d("JWP" + "jornals",
                            //        cur.getString(cur.getColumnIndex("name")));

                            if (file.exists()) {
                                file_isn = 1;
                            } else {
                                Log.d("JWP" + "jornals",
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

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                Log.e("JWP", "img - " + img.toString());

                if (calendar.get(Calendar.YEAR) != yer || calendar.get(Calendar.MONTH) != mon) {
                    yer = calendar.get(Calendar.YEAR);
                    mon = calendar.get(Calendar.MONTH);
                    m = new HashMap<String, String>();
                    m.put("groupName", main.funct.getMonth(mon));
                    groupData.add(m);

                    if (i > 0) {
                        childData.add(childDataItem);
                        childDataItem = new ArrayList<Map<String, String>>();
                    }

                    m = new HashMap<String, String>();
                    m.put("name", name);
                    m.put("code_pub", code_pub);
                    m.put("code_lng", code_lng);
                    m.put("_id", _id.toString());
                    m.put("img", img.toString());
                    m.put("id_type", files);
                    childDataItem.add(m);
                } else {
                    m = new HashMap<String, String>();
                    m.put("name", name);
                    m.put("code_pub", code_pub);
                    m.put("code_lng", code_lng);
                    m.put("_id", _id.toString());
                    m.put("img", img.toString());
                    m.put("id_type", files);
                    childDataItem.add(m);
                }
                cursor.moveToNext();
            }
            childData.add(childDataItem);
            class_rss_jornals_adapter adapter = new class_rss_jornals_adapter(
                    getActivity(), groupData, childData, main.database);
            list.setAdapter(adapter);
        } catch (Exception e) {
            main.funct.send_bug_report(getActivity(), e, "jornals",
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