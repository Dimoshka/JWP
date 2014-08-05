package ua.pp.dimoshka.classes;

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

import java.io.File;

import ua.pp.dimoshka.jwp.R;

public class class_books_brochures_adapter extends SimpleCursorAdapter {

    private int layout;
    private class_functions funct;
    private SQLiteDatabase database;

    public class_books_brochures_adapter(Context context,
                                         String[] from, int[] to,
                                         SQLiteDatabase database, class_functions funct) {
        super(context, R.layout.list_items_books_brochures, null, from, to, 0);
        this.layout = R.layout.list_items_books_brochures;
        this.database = database;
        this.funct = funct;
    }

    @Override
    public void bindView(View v, Context context, Cursor c) {
        try {
            super.bindView(v, context, c);
            AQuery aq = new AQuery(v);

            Boolean img = Boolean.valueOf(c.getInt(c.getColumnIndex("img")) != 0);
            Integer _id = Integer.valueOf(c.getInt(c.getColumnIndex("_id")));
            String name = c.getString(c.getColumnIndex("name"));
            String title = c.getString(c.getColumnIndex("title"));

            String[] id_type_files = null;
            String[] file_files = null;
            if (!c.isNull(c.getColumnIndex("id_type_files")) && !c.isNull(c.getColumnIndex("file_files"))) {
                id_type_files = c.getString(c.getColumnIndex("id_type_files")).split(",");
                file_files = c.getString(c.getColumnIndex("file_files")).split(",");
            }

            aq.id(R.id.title).text(title);

            if (img.booleanValue()) {
                if (funct.ExternalStorageState()) {
                    File imgFile = new File(funct.get_dir_app() + "/img/books_brochures/"
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
            } else {
                aq.id(R.id.img).image(R.drawable.ic_noimages);
            }

            aq.id(R.id.epub).image(R.drawable.ic_none_type);
            aq.id(R.id.pdf).image(R.drawable.ic_none_type);
            aq.id(R.id.mobi).image(R.drawable.ic_none_type);
            aq.id(R.id.mp3).image(R.drawable.ic_none_type);
            aq.id(R.id.aac).image(R.drawable.ic_none_type);

            if (id_type_files != null) {
                for (int i = 0; i < id_type_files.length; i++) {
                    Boolean file_isn = Boolean.valueOf(Integer.parseInt(file_files[i]) != 0);
                    switch (Integer.parseInt(id_type_files[i])) {
                        case 1:
                            if (file_isn.booleanValue())
                                aq.id(R.id.epub).image(R.drawable.ic_epub_1);
                            else
                                aq.id(R.id.epub).image(R.drawable.ic_epub_0);
                            break;
                        case 2:
                            if (file_isn.booleanValue())
                                aq.id(R.id.pdf).image(R.drawable.ic_pdf_1);
                            else
                                aq.id(R.id.pdf).image(R.drawable.ic_pdf_0);
                            break;
                        case 3:
                            if (file_isn.booleanValue())
                                aq.id(R.id.mp3).image(R.drawable.ic_mp3_1);
                            else
                                aq.id(R.id.mp3).image(R.drawable.ic_mp3_0);
                            break;
                        case 4:
                            if (file_isn.booleanValue())
                                aq.id(R.id.aac).image(R.drawable.ic_aac_1);
                            else
                                aq.id(R.id.aac).image(R.drawable.ic_aac_0);
                            break;
                        case 6:
                            if (file_isn.booleanValue())
                                aq.id(R.id.mobi).image(R.drawable.ic_mobi_1);
                            else
                                aq.id(R.id.mobi).image(R.drawable.ic_mobi_0);
                            break;
                        default:
                            break;
                    }
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