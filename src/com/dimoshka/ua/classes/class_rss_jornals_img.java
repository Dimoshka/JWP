package com.dimoshka.ua.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.dimoshka.ua.jwp.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class class_rss_jornals_img {

    static final String URL_IMG = "http://www.jw.org/assets/a/{code_pub_shot}{YY}/{YYYYMMDD}/{code_pub_shot}{YY}_{YYYYMMDD}_{code_lng}/{code_pub}_{code_lng}_{YYYYMMDD}.prd_md.jpg";
    // http://www.jw.org/assets/a/g13/201303/g13_201303_U/g_U_201303prd_md.jpg
    // http://www.jw.org/assets/a/g13/201307/g13_201307_U/g_U_201307.prd_md.jpg

    private SQLiteDatabase database;
    public class_functions funct = new class_functions();
    private Activity activity;
    private Cursor cursor;
    private Handler handler;

    public class_rss_jornals_img(Activity activity, Handler handler,
                                 SQLiteDatabase database) {
        this.activity = activity;
        this.handler = handler;
        this.database = database;
    }

    @SuppressWarnings("deprecation")
	public void verify_all_img() {
        cursor = database
                .rawQuery(
                        "select magazine._id, magazine.name, magazine.img, language.code as code_lng, publication.code as code_pub, publication._id as cur_pub from magazine left join language on magazine.id_lang=language._id left join publication on magazine.id_pub=publication._id where img=0 and magazine.id_pub BETWEEN '1' and '3'",
                        null);
        activity.startManagingCursor(cursor);
        new verify_img().execute();
    }

    class verify_img extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog;
        List<class_rss_item> rss_list = null;

        @SuppressLint("SimpleDateFormat")
        protected Void doInBackground(Void... paramArrayOfVoid) {
            try {
                if (funct.ExternalStorageState()) {
                    cursor.moveToFirst();
                    String dir = funct.get_dir_app(activity) + "/img/";

                    File Directory = new File(dir);
                    if (!Directory.isDirectory()) {
                        Directory.mkdirs();
                    }

                    for (int i = 0; i < cursor.getCount(); i++) {
                        String name = cursor.getString(cursor
                                .getColumnIndex("name"));
                        String code_lng = cursor.getString(cursor
                                .getColumnIndex("code_lng"));
                        String code_pub = cursor.getString(cursor
                                .getColumnIndex("code_pub"));
                        Integer cur_pub = cursor.getInt(cursor
                                .getColumnIndex("cur_pub"));
                        ContentValues initialValues = new ContentValues();

                        File imgFile = new File(dir + "/img/" + name + ".jpg");
                        if (!imgFile.exists()) {
                            Log.i("JWP_image", name + " - no found!");
                            SimpleDateFormat format = new SimpleDateFormat(
                                    "yyyyMMdd");
                            Date date = funct.get_jwp_jornals_rss_date(name,
                                    code_pub, code_lng);

                            String url_str = URL_IMG;

                            if (cur_pub == 2)
                                url_str = url_str.replace("{code_pub_shot}",
                                        "w");
                            else
                                url_str = url_str.replace("{code_pub_shot}",
                                        code_pub);
                            url_str = url_str.replace("{code_pub}", code_pub);
                            url_str = url_str.replace("{code_lng}", code_lng);
                            format.applyPattern("yy");
                            url_str = url_str.replace("{YY}",
                                    format.format(date));
                            if (cur_pub == 3)
                                format.applyPattern("yyyyMM");
                            else
                                format.applyPattern("yyyyMMdd");
                            url_str = url_str.replace("{YYYYMMDD}",
                                    format.format(date));

                            try {

                                if (funct.load_img(activity, dir, name, url_str)) {
                                    Log.i("JWP_image", name
                                            + " - file download complete!");
                                    initialValues.put("img", "1");
                                    String[] args = {String
                                            .valueOf(cursor.getString(cursor
                                                    .getColumnIndex("_id")))};
                                    database.update("magazine", initialValues,
                                            "_id=?", args);
                                }
                            } catch (Exception e) {
                                Log.e("JWP_" + getClass().getName(),
                                        e.toString());
                            }
                        } else {
                            Log.i("JWP_image", name + " found!");
                            initialValues.put("img", "1");
                            String[] args = {String.valueOf(cursor
                                    .getString(cursor.getColumnIndex("_id")))};
                            database.update("magazine", initialValues, "_id=?",
                                    args);
                        }
                        cursor.moveToNext();
                    }
                }
            } catch (Exception e) {
                funct.send_bug_report(activity, e, getClass().getName(), 154);
            }
            return null;
        }

        @SuppressWarnings("deprecation")
		protected void onPostExecute(Void result) {
            this.dialog.hide();
            activity.stopManagingCursor(cursor);
            cursor.close();
            handler.sendEmptyMessage(2);
        }

        protected void onPreExecute() {
            this.dialog = ProgressDialog
                    .show(activity,
                            null,
                            activity.getResources().getString(
                                    R.string.dialog_loaing_img), true);
        }
    }

}
