package com.dimoshka.ua.jwp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.dimoshka.ua.classes.class_news_adapter;

public class news extends ListFragment {

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
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri data = Uri.parse(cursor.getString(cursor.getColumnIndex("link")));
        intent.setDataAndType(data, "text/html");
        startActivity(intent);
    }

    public void refresh() {
        try {
            cursor = main.database.rawQuery(
                    "select * from news where news.id_lang='" + main.id_lang
                            + "' order by pubdate desc, news._id asc", null
            );
            class_news_adapter adapter = new class_news_adapter(
                    getActivity(), R.layout.list_items_news_img, cursor, new String[]{}, new int[]{}, 0, main.database, main.funct);
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