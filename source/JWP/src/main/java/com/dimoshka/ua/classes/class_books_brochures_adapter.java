package com.dimoshka.ua.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.dimoshka.ua.jwp.R;
import com.dimoshka.ua.jwp.main;

import java.io.File;
import java.util.ArrayList;

public class class_books_brochures_adapter extends SimpleCursorAdapter {

    private int layout;

    private class_functions funct = new class_functions();
    private SQLiteDatabase database;
    private ArrayList<String> files_arr;


    @SuppressWarnings("deprecation")
    public class_books_brochures_adapter(Context context, int layout,
                                         Cursor c, String[] from, int[] to,
                                         SQLiteDatabase database, ArrayList<String> files_arr) {
        super(context, layout, c, from, to);
        this.layout = layout;
        this.database = database;
        this.files_arr = files_arr;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
        try {
            Integer img = c.getInt(c.getColumnIndex("img"));
            Integer _id = c.getInt(c.getColumnIndex("_id"));
            //String files = c.getString(c.getColumnIndex("id_type"));
            String name = c.getString(c.getColumnIndex("name"));
            String title = c.getString(c.getColumnIndex("title"));
            String[] id_types = files_arr.get(c.getPosition()).split(",");

            TextView titlet = (TextView) v.findViewById(R.id.title);
            titlet.setText(title);

            ImageView myImage = (ImageView) v.findViewById(R.id.img);
            if (img == 1) {
                File imgFile = new File(funct.get_dir_app(context) + "/img/"
                        + name + ".jpg");
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
                            .getAbsolutePath());
                    myImage.setImageBitmap(myBitmap);
                } else {
                    myImage.setImageResource(R.drawable.ic_noimages);
                    ContentValues initialValues = new ContentValues();
                    initialValues.put("img", "0");
                    database.update("magazine", initialValues, "_id=?",
                            new String[]{_id.toString()});
                }
            } else {
                myImage.setImageResource(R.drawable.ic_noimages);
            }

            ImageView epub = (ImageView) v.findViewById(R.id.epub);
            ImageView pdf = (ImageView) v.findViewById(R.id.pdf);
            ImageView mp3 = (ImageView) v.findViewById(R.id.mp3);
            ImageView aac = (ImageView) v.findViewById(R.id.aac);
            epub.setImageResource(R.drawable.ic_none_type);
            pdf.setImageResource(R.drawable.ic_none_type);
            mp3.setImageResource(R.drawable.ic_none_type);
            aac.setImageResource(R.drawable.ic_none_type);


            for (int i = 0; i < id_types.length; i++) {

                String[] f = id_types[i].split("=");
                int file_isn = Integer.parseInt(f[1]);

                switch (Integer.parseInt(f[0])) {
                    case 1:
                        if (file_isn == 1)
                            epub.setImageResource(R.drawable.ic_epub_1);
                        else
                            epub.setImageResource(R.drawable.ic_epub_0);
                        break;
                    case 2:

                        if (file_isn == 1)
                            pdf.setImageResource(R.drawable.ic_pdf_1);
                        else
                            pdf.setImageResource(R.drawable.ic_pdf_0);
                        break;
                    case 3:
                        if (file_isn == 1)
                            mp3.setImageResource(R.drawable.ic_mp3_1);
                        else
                            mp3.setImageResource(R.drawable.ic_mp3_0);
                        break;
                    case 4:
                        if (file_isn == 1)
                            aac.setImageResource(R.drawable.ic_aac_0);
                        else
                            aac.setImageResource(R.drawable.ic_aac_0);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            main.funct.send_bug_report(context, e, getClass().getName(),
                    128);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(layout, parent, false);
    }
}