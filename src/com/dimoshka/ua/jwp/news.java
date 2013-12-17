package com.dimoshka.ua.jwp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.dimoshka.ua.classes.class_rss_news_adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class news extends SherlockFragment {

    private ExpandableListView list;
    private Cursor cursor;
    private ArrayList<Map<String, String>> groupData;
    private ArrayList<Map<String, String>> childDataItem;
    private ArrayList<ArrayList<Map<String, String>>> childData;
    private Map<String, String> m;

    @Nullable
    View view = null;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup group,
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

                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);

                    Uri data = Uri.parse(hash.get("link"));
                    intent.setDataAndType(data, "text/html");
                    // Intent ch = Intent.createChooser(intent,
                    // getString(R.string.select));
                    // startActivity(ch);
                    startActivity(intent);
                    return false;
                }
            });

        } catch (Exception e) {
            main.funct.send_bug_report(getActivity(), e, "news",
                    102);
        }
        refresh();

        return view;
    }


    public void refresh() {
        try {
            cursor = main.database.rawQuery(
                    "select * from news where news.id_lang='" + main.id_lang
                            + "' order by pubdate desc, news._id asc", null);
            groupData = new ArrayList<Map<String, String>>();
            childData = new ArrayList<ArrayList<Map<String, String>>>();
            childDataItem = new ArrayList<Map<String, String>>();

            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {

                childDataItem = new ArrayList<Map<String, String>>();

                Integer _id = cursor.getInt(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String link = cursor.getString(cursor.getColumnIndex("link"));
                String description = cursor.getString(cursor
                        .getColumnIndex("description"));
                String pubdate = cursor.getString(cursor
                        .getColumnIndex("pubdate"));

                Integer img = cursor.getInt(cursor.getColumnIndex("img"));

                m = new HashMap<String, String>();
                m.put("groupName", title);
                groupData.add(m);

                m = new HashMap<String, String>();

                m.put("_id", _id.toString());
                m.put("name", "news_" + _id.toString());
                m.put("title", title);
                m.put("link", link);
                m.put("description", description);
                m.put("pubdate", pubdate);
                m.put("img", img.toString());

                childDataItem.add(m);
                childData.add(childDataItem);
                cursor.moveToNext();
            }
            class_rss_news_adapter adapter = new class_rss_news_adapter(
                    getActivity(), groupData, childData, main.database);
            list.setAdapter(adapter);
        } catch (Exception e) {
            main.funct.send_bug_report(getActivity(), e, "news",
                    182);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
