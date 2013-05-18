package com.dimoshka.ua.classes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.dimoshka.ua.jwp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class class_rss_books_brochures_adapter extends SimpleCursorAdapter {

    private int layout;
    private String[] from;
    private int[] to;
    public class_functions funct = new class_functions();
    private SQLiteDatabase database;


    @SuppressWarnings("deprecation")
    public class_rss_books_brochures_adapter(Context context, int layout,
                                             Cursor c, String[] from, int[] to,
                                             SQLiteDatabase database) {
        super(context, layout, c, from, to);
        this.layout = layout;
        this.from = from;
        this.to = to;
        this.database = database;

    }

    @Override
    public void bindView(View v, Context context, Cursor c) {

        Integer img = c.getInt(c.getColumnIndex("img"));
        Integer _id = c.getInt(c.getColumnIndex("_id"));
        String files = c.getString(c.getColumnIndex("id_type"));
        String name = c.getString(c.getColumnIndex("name"));
        String[] id_types = files.split(",");

        TextView title = (TextView) v.findViewById(R.id.title);
        title.setText(name);

        ImageView myImage = (ImageView) v.findViewById(R.id.img);
        if (img == 1) {
            File imgFile = new File(funct.get_dir_app(context) + "/img/"
                    + name + ".jpg");
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
                        .getAbsolutePath());
                myImage.setImageBitmap(myBitmap);
            } else {
                myImage.setImageResource(R.drawable.noimages);
                ContentValues initialValues = new ContentValues();
                initialValues.put("img", "0");
                database.update("magazine", initialValues, "_id=?",
                        new String[]{_id.toString()});
            }
        } else {
            myImage.setImageResource(R.drawable.noimages);
        }

        for (int i = 0; i < id_types.length; i++) {

            String[] f = id_types[i].split("=");
            int file_isn = Integer.parseInt(f[1]);

            switch (Integer.parseInt(f[0])) {
                case 1:
                    ImageView epub = (ImageView) v.findViewById(R.id.epub);
                    if (file_isn == 1)
                        epub.setImageResource(R.drawable.epub_1);
                    else
                        epub.setImageResource(R.drawable.epub_0);
                    break;
                case 2:

                    ImageView pdf = (ImageView) v.findViewById(R.id.pdf);
                    if (file_isn == 1)
                        pdf.setImageResource(R.drawable.pdf_1);
                    else
                        pdf.setImageResource(R.drawable.pdf_0);
                    break;
                case 3:
                    ImageView mp3 = (ImageView) v.findViewById(R.id.mp3);
                    if (file_isn == 1)
                        mp3.setImageResource(R.drawable.mp3_1);
                    else
                        mp3.setImageResource(R.drawable.mp3_0);
                    break;
                case 4:
                    ImageView aac = (ImageView) v.findViewById(R.id.aac);
                    if (file_isn == 1)
                        aac.setImageResource(R.drawable.aac_0);
                    else
                        aac.setImageResource(R.drawable.aac_0);
                    break;
                default:
                    break;
            }
        }


        for (int i = 0; i < from.length; i++) {
            TextView t = (TextView) v.findViewById(to[i]);
            t.setText(c.getString(c.getColumnIndex(from[i])));

        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layout, parent, false);
    }
}

