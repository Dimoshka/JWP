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
import java.util.Map;

import ua.pp.dimoshka.classes.class_functions;
import ua.pp.dimoshka.jwp.R;

public class video_adapter extends SimpleCursorAdapter {

    private int layout;
    private class_functions funct;
    private SQLiteDatabase database;

    public video_adapter(Context context,
                         String[] from, int[] to,
                         SQLiteDatabase database, class_functions funct) {
        super(context, R.layout.list_items_video, null, from, to, 0);
        this.layout = R.layout.list_items_video;
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


            if (Boolean.valueOf(c.getInt(c
                    .getColumnIndex("favorite")) != 0))
                aq.id(R.id.item).backgroundColor(v.getResources().getColor(R.color.main_lite));
            else aq.id(R.id.item).backgroundColor(v.getResources().getColor(R.color.white));

            aq.id(R.id.title).text(title);
            //aq.id(R.id.text).text(name);

            if (img.booleanValue()) {
                if (funct.ExternalStorageState()) {
                    File imgFile = new File(funct.get_dir_app() + "/img/video/"
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

            if (id_type_files != null) {
                Map<Integer, Boolean> tipe_files = funct.get_map_files(id_type_files, file_files);
                for (Integer key : tipe_files.keySet()) {
                    switch (key) {
                        case 11:
                            if (tipe_files.get(key).booleanValue())
                                aq.id(R.id.type1).image(R.drawable.ic_mp4_1);
                            else
                                aq.id(R.id.type1).image(R.drawable.ic_mp4_0);
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