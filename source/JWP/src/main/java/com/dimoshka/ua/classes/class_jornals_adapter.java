package com.dimoshka.ua.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.dimoshka.ua.jwp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class class_jornals_adapter extends SimpleCursorAdapter {

    private int layout;
    private class_functions funct;
    private SQLiteDatabase database;

    public class_jornals_adapter(Context context, int layout,
                                 Cursor c, String[] from, int[] to, int flags,
                                 SQLiteDatabase database, class_functions funct) {
        super(context, layout, c, from, to, flags);
        this.layout = layout;
        this.database = database;
        this.funct = funct;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
        try {
            super.bindView(v, context, c);
            AQuery aq = new AQuery(v);
            String name = c.getString(c.getColumnIndex("name"));
            Date date = funct.get_string_to_date(c.getString(c.getColumnIndex("date")), "yyyy-MM-dd");
            Boolean img = c.getInt(c.getColumnIndex("img")) != 0;
            Integer _id = c.getInt(c.getColumnIndex("_id"));
            SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");

            String[] id_type_files = c.getString(c.getColumnIndex("id_type_files")).split(",");
            //String[] name_files = c.getString(c.getColumnIndex("name_files")).split(",");
            String[] file_files = c.getString(c.getColumnIndex("file_files")).split(",");

            aq.id(R.id.title).text(format.format(date));
            aq.id(R.id.text).text(name);

            if (img) {
                File imgFile = new File(funct.get_dir_app() + "/img/"
                        + name + ".jpg");

                if (imgFile.exists()) {
                    aq.id(R.id.img).image(imgFile, false, 78, new BitmapAjaxCallback() {
                        @Override
                        public void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
                            iv.setImageBitmap(bm);
                        }
                    });
                } else {
                    aq.id(R.id.img).image(R.drawable.ic_noimages);

                    ContentValues initialValues = new ContentValues();
                    initialValues.put("img", "0");
                    database.update("magazine", initialValues, "_id=?",
                            new String[]{_id.toString()});
                }
            } else {
                aq.id(R.id.img).image(R.drawable.ic_noimages);
            }

            aq.id(R.id.epub).image(R.drawable.ic_none_type);
            aq.id(R.id.pdf).image(R.drawable.ic_none_type);
            aq.id(R.id.mp3).image(R.drawable.ic_none_type);
            aq.id(R.id.aac).image(R.drawable.ic_none_type);

            for (int i = 0; i < id_type_files.length; i++) {
                Boolean file_isn = Integer.parseInt(file_files[i]) != 0;
                switch (Integer.parseInt(id_type_files[i])) {
                    case 1:
                        if (file_isn)
                            aq.id(R.id.epub).image(R.drawable.ic_epub_1);
                        else
                            aq.id(R.id.epub).image(R.drawable.ic_epub_0);
                        break;
                    case 2:
                        if (file_isn)
                            aq.id(R.id.pdf).image(R.drawable.ic_pdf_1);
                        else
                            aq.id(R.id.pdf).image(R.drawable.ic_pdf_0);
                        break;
                    case 3:
                        if (file_isn)
                            aq.id(R.id.mp3).image(R.drawable.ic_mp3_1);
                        else
                            aq.id(R.id.mp3).image(R.drawable.ic_mp3_0);
                        break;
                    case 4:
                        if (file_isn)
                            aq.id(R.id.aac).image(R.drawable.ic_aac_1);
                        else
                            aq.id(R.id.aac).image(R.drawable.ic_aac_0);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layout, parent, false);
    }
}
