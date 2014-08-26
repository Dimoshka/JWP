package ua.pp.dimoshka.jwp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.ListView;

import ua.pp.dimoshka.adapter.news_adapter;
import ua.pp.dimoshka.classes.my_ListFragment;

public class news extends my_ListFragment {

    public void setadapter_list() {
        mAdapter = new news_adapter(
                getActivity(), new String[]{}, new int[]{}, main.get_database(), main.get_funct());
        sqlite_rawQuery = "select * from news where news.id_lang='" + main.get_funct().get_id_lng() + "' order by news._id DESC;";
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri data = Uri.parse(cursor.getString(cursor.getColumnIndex("link")));
        intent.setDataAndType(data, "text/html");
        startActivity(intent);
    }
}