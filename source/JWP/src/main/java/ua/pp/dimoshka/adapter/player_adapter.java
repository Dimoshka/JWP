package ua.pp.dimoshka.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;

import ua.pp.dimoshka.jwp.R;

public class player_adapter extends SimpleCursorAdapter {
    private int layout;
    private String[] from;
    private int[] to;

    public player_adapter(Context context,
                          String[] from, int[] to) {
        super(context, android.R.layout.simple_list_item_single_choice, null, from, to, 0);
        this.layout = android.R.layout.simple_list_item_single_choice;
        this.from = from;
        this.to = to;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
        super.bindView(v, context, c);
        AQuery aq = new AQuery(v);

        for (int i = 0; i < from.length; i++) {
            aq.id(to[i]).text(c.getString(c.getColumnIndex(from[i])));
            aq.id(to[i]).textSize(12);

            if (c.getInt(c.getColumnIndex("file")) == 1) {
                aq.id(to[i]).textColor(context.getResources().getColor(
                        R.color.text_dark_blue));
            } else {
                aq.id(to[i]).textColor(context.getResources().getColor(
                        R.color.text_green));
            }
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layout, parent, false);
    }
}
