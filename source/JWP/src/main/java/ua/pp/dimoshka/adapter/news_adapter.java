package ua.pp.dimoshka.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;

import java.io.File;

import ua.pp.dimoshka.classes.class_functions;
import ua.pp.dimoshka.jwp.R;

public class news_adapter extends SimpleCursorAdapter {
    private class_functions funct;
    private SQLiteDatabase database;

    public news_adapter(Context context,
                        String[] from, int[] to,
                        SQLiteDatabase database, class_functions funct) {
        super(context, R.layout.list_items_news, null, from, to, 0);
        this.database = database;
        this.funct = funct;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
        try {
            super.bindView(v, context, c);
            AQuery aq = new AQuery(v);
            String titles = c.getString(c.getColumnIndex("title"));
            String description = c.getString(c.getColumnIndex("description"));
            String pubdate = c.getString(c.getColumnIndex("pubdate"));
            Boolean img = Boolean.valueOf(c.getInt(c.getColumnIndex("img")) != 0);
            Integer _id = Integer.valueOf(c.getInt(c.getColumnIndex("_id")));

            aq.id(R.id.title).text(titles);
            aq.id(R.id.text).text(description);
            aq.id(R.id.date).text(pubdate);

            if (img.booleanValue()) {
                if (funct.ExternalStorageState()) {
                    File imgFile = new File(funct.get_dir_app() + "/img/news/"
                            + _id + ".jpg");

                    if (imgFile.exists()) {
                        BitmapAjaxCallback cb = new BitmapAjaxCallback();
                        cb.targetWidth(0).round(10);
                        aq.id(R.id.img).image(imgFile, false, 0, cb);

                    } else {
                        aq.id(R.id.img).image(R.drawable.ic_noimages);
                        ContentValues initialValues = new ContentValues();
                        initialValues.put("img", "0");
                        database.update("news", initialValues, "_id=?",
                                new String[]{_id.toString()});
                    }
                } else {
                    aq.id(R.id.img).image(R.drawable.ic_noimages);
                }
            } else {
                //aq.id(R.id.img).image(R.drawable.ic_noimages);
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    @Override
    public View newView(Context context, Cursor c, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        Boolean img = Boolean.valueOf(c.getInt(c.getColumnIndex("img")) != 0);
        if (img.booleanValue()) {
            return inflater.inflate(R.layout.list_items_news, parent, false);
        } else {
            return inflater.inflate(R.layout.list_items_news_noimg, parent, false);
        }
    }

}
