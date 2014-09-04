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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import ua.pp.dimoshka.classes.class_functions;
import ua.pp.dimoshka.jwp.R;

public class journals_adapter extends SimpleCursorAdapter {

    private int layout;
    private class_functions funct;
    private SQLiteDatabase database;

    public journals_adapter(Context context,
                            String[] from, int[] to,
                            SQLiteDatabase database, class_functions funct) {
        super(context, R.layout.list_items_journals, null, from, to, 0);
        this.layout = R.layout.list_items_journals;
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
            Boolean img = Boolean.valueOf(c.getInt(c.getColumnIndex("img")) != 0);
            Integer _id = Integer.valueOf(c.getInt(c.getColumnIndex("_id")));
            SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");

            String[] id_type_files = c.getString(c.getColumnIndex("id_type_files")).split(",");
            String[] file_files = c.getString(c.getColumnIndex("file_files")).split(",");

            if (Boolean.valueOf(c.getInt(c
                    .getColumnIndex("favorite")) != 0))
                aq.id(R.id.item).backgroundColor(v.getResources().getColor(R.color.main_lite));
            else aq.id(R.id.item).backgroundColor(v.getResources().getColor(R.color.white));

            aq.id(R.id.title).text(format.format(date));
            aq.id(R.id.text).text(name);

            if (img.booleanValue()) {
                if (funct.ExternalStorageState()) {
                    File imgFile = new File(funct.get_dir_app() + "/img/journals/"
                            + name + ".jpg");

                    if (imgFile.exists()) {

                        BitmapAjaxCallback cb = new BitmapAjaxCallback();
                        cb.targetWidth(0).round(10);
                        aq.id(R.id.img).image(imgFile, false, 0, cb);


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

            aq.id(R.id.type1).image(R.drawable.ic_none_type);
            aq.id(R.id.type2).image(R.drawable.ic_none_type);
            aq.id(R.id.type3).image(R.drawable.ic_none_type);
            aq.id(R.id.type4).image(R.drawable.ic_none_type);
            aq.id(R.id.type5).image(R.drawable.ic_none_type);


            if (id_type_files != null) {
                Map<Integer, Boolean> tipe_files = funct.get_map_files(id_type_files, file_files);
                for (Integer key : tipe_files.keySet()) {
                    switch (key) {
                        case 1:
                            if (tipe_files.get(key).booleanValue())
                                aq.id(R.id.type1).image(R.drawable.ic_pdf_1);
                            else
                                aq.id(R.id.type1).image(R.drawable.ic_pdf_0);
                            break;
                        case 2:
                            if (tipe_files.get(key).booleanValue())
                                aq.id(R.id.type2).image(R.drawable.ic_epub_1);
                            else
                                aq.id(R.id.type2).image(R.drawable.ic_epub_0);
                            break;
                        case 3:
                            if (tipe_files.get(key).booleanValue())
                                aq.id(R.id.type3).image(R.drawable.ic_mobi_1);
                            else
                                aq.id(R.id.type3).image(R.drawable.ic_mobi_0);
                            break;

                        case 6:
                            if (tipe_files.get(key).booleanValue())
                                aq.id(R.id.type4).image(R.drawable.ic_mp3_1);
                            else
                                aq.id(R.id.type4).image(R.drawable.ic_mp3_0);
                            break;

                        case 7:
                            if (tipe_files.get(key).booleanValue())
                                aq.id(R.id.type5).image(R.drawable.ic_aac_1);
                            else
                                aq.id(R.id.type5).image(R.drawable.ic_aac_0);
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
