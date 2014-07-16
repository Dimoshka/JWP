package com.dimoshka.ua.classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dimoshka.ua.jwp.R;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class class_rss_jornals {
    static final String URL_FEED = "http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=%s&rmn=%s&rfm=%s&rpf=&rpe=";
    static final String URL_IMG = "http://assets.jw.org/assets/a/{code_pub_shot}{YY}/{YYYYMMDD}/{code_pub_shot}{YY}_{YYYYMMDD}_{code_lng}/{code_pub}_{code_lng}_{YYYYMMDD}_md.jpg";

    private class_rss_provider rssfeedprovider;
    private SQLiteDatabase database;

    public class_functions funct;
    private Activity activity;
    private Handler handler;
    private Integer id_ln = 0;

    private String code_lng = "E";

    private ArrayList<Integer> id_pub = new ArrayList<Integer>();

    private ArrayList<String> code_pub = new ArrayList<String>();
    private Integer cur_pub = 0;

    private ArrayList<Integer> id_type = new ArrayList<Integer>();

    private ArrayList<String> code_type = new ArrayList<String>();
    private Integer cur_type = 0;
    public SharedPreferences prefs;
    private AsyncTask task;

    public class_rss_jornals(Activity activity, int id_lang, Handler handler,
                             SQLiteDatabase database, class_functions funct) {
        this.activity = activity;
        this.handler = handler;
        this.database = database;
        this.funct = funct;
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        get_language(id_lang);
        get_publication();

    }

    public void get_all_feeds() {
        try {
            task = new ReadFeedTask().execute();
        } catch (Exception e) {
            Log.e("JWP_" + getClass().getName(), e.toString());
        }
    }

    public Integer get_language(int id) {
        Cursor cursor = funct.get_language(database, id);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            id_ln = cursor.getInt(cursor.getColumnIndex("_id"));
            code_lng = cursor.getString(cursor.getColumnIndex("code"));
        } else {
            id_ln = 1;
            code_lng = "E";
        }
        return id_ln;
    }

    private void get_publication() {
        try {
            Cursor cursor_type = database.query("type", new String[]{"_id",
                    "code"}, null, null, null, null, "_id");
            Cursor cursor_pub = database.query("publication", new String[]{
                    "_id", "code"}, null, null, null, null, "_id");
            cursor_type.moveToFirst();
            cursor_pub.moveToFirst();

            for (int i = 0; i < cursor_type.getCount(); i++) {
                id_type.add(cursor_type.getInt(cursor_type
                        .getColumnIndex("_id")));
                code_type.add(cursor_type.getString(cursor_type
                        .getColumnIndex("code")));
                cursor_type.moveToNext();
            }

            for (int i = 0; i < cursor_pub.getCount(); i++) {
                id_pub.add(cursor_pub.getInt(cursor_pub.getColumnIndex("_id")));
                code_pub.add(cursor_pub.getString(cursor_pub
                        .getColumnIndex("code")));
                cursor_pub.moveToNext();
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    class ReadFeedTask extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog;

        List<class_rss_item> rss_list = null;

        @Override
        protected Void doInBackground(Void... paramArrayOfVoid) {
            try {
                for (int a = 0; a < id_pub.size(); a++) {
                    if (isCancelled()) {
                        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                        if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            Log.e("JWP", "isCancelled+");
                            if (dialog != null)
                                dialog.dismiss();
                            Log.e("JWP", "onPostExecute+");
                            handler.sendEmptyMessage(1);
                        }
                        break;
                    }
                    for (int b = 0; b < id_type.size(); b++) {
                        Boolean load_pub = false;
                        String s = code_type.get(b);
                        if (s.equals("epub")) {
                            load_pub = prefs.getBoolean("rss_epub", false);
                        } else if (s.equals("pdf")) {
                            load_pub = prefs.getBoolean("rss_pdf", true);

                        } else if (s.equals("mp3")) {
                            load_pub = prefs.getBoolean("rss_mp3", false);

                        } else if (s.equals("m4b")) {
                            load_pub = prefs.getBoolean("rss_m4b", false);

                        }

                        if (load_pub) {
                            //Log.i("JWP_rss", "rss_" + code_type.get(b));

                            cur_type = b;
                            cur_pub = a;
                            rssfeedprovider = new class_rss_provider();
                            String feed = String.format(URL_FEED, code_lng,
                                    code_pub.get(cur_pub),
                                    code_type.get(cur_type));
                            this.rss_list = rssfeedprovider.parse(feed,
                                    activity);

                            for (int i = 0; i < rss_list.size(); i++) {

                                class_rss_item rss_item = rss_list.get(i);

                                String name = rss_item.getguid();
                                name = name.replace(
                                        "." + code_type.get(cur_type), "");

                                SimpleDateFormat sim_format = new SimpleDateFormat(
                                        "yyyy-MM-dd");
                                DateFormat dat_format = new SimpleDateFormat(
                                        "yyyy-MM-dd");
                                Date date = funct.get_jwp_jornals_rss_date(
                                        name, code_pub.get(cur_pub), code_lng);
                                Log.e("JWP_rss", "date = " + date.toString());
                                String date_str = name.replace(
                                        code_pub.get(cur_pub) + "_", "");
                                date_str = date_str.replace(code_lng + "_", "");
                                Log.e("JWP_rss", "name = " + name);
                                if (date_str.length() > 8)
                                    name = name.substring(0, name.length() - 3);
                                Cursor cur = database.rawQuery(
                                        "select _id, img from magazine where `name` = '"
                                                + name + "'", null
                                );
                                long id_magazine = 0;
                                Integer img = img(name, sim_format, date);
                                Log.e("JWP", "img_ok - " + img.toString());
                                if (cur.getCount() > 0) {
                                    cur.moveToFirst();
                                    id_magazine = cur.getLong(cur
                                            .getColumnIndex("_id"));
                                    if (img != cur.getInt(cur
                                            .getColumnIndex("img"))) {
                                        ContentValues init = new ContentValues();
                                        init.put("img", img);
                                        Log.e("JWP_img", "update img to " + img.toString());
                                        database.update("magazine", init, "_id=?",
                                                new String[]{String.valueOf(id_magazine)});
                                    }
                                } else {
                                    ContentValues init1 = new ContentValues();
                                    init1.put("name", name);
                                    init1.put("name", name);
                                    init1.put("id_pub", id_pub.get(cur_pub));
                                    init1.put("id_lang", id_ln);
                                    init1.put("img", img);
                                    init1.put("date", dat_format.format(date));
                                    Log.e("JWP_rss", "date_ok = " + dat_format.format(date).toString());
                                    id_magazine = database.insert("magazine",
                                            null, init1);
                                }

                                ContentValues init2 = new ContentValues();
                                init2.put("id_magazine", id_magazine);
                                init2.put("id_type", id_type.get(cur_type));
                                init2.put("name", rss_item.getguid());
                                init2.put("link", rss_item.getLink());
                                init2.put("pubdate", rss_item.getPubDate());
                                init2.put("title", rss_item.getTitle());
                                init2.put("file", 0);

                                database.insertWithOnConflict("files", null,
                                        init2, SQLiteDatabase.CONFLICT_IGNORE);

                            }
                        }
                    }
                }

            } catch (Exception e) {
                funct.send_bug_report(e);
            }
            return null;
        }

        protected Integer img(String name, SimpleDateFormat format, Date date) {
            Integer img = 0;
            if (prefs.getBoolean("downloads_img", true)) {
                if (funct.ExternalStorageState()) {
                    String dir = funct.get_dir_app() + "/img/";
                    File Directory = new File(dir);
                    if (!Directory.isDirectory()) {
                        Directory.mkdirs();
                    }
                    File imgFile = new File(dir + "/img/" + name + ".jpg");
                    if (!imgFile.exists()) {
                        Log.i("JWP_image", name + " - not found!");
                        String code_pub = "";
                        switch (cur_pub) {
                            case 0:
                                code_pub = "w";
                                break;
                            case 1:
                                code_pub = "wp";
                                break;
                            case 2:
                                code_pub = "g";
                                break;
                        }
                        String url_str = URL_IMG;
                        if (cur_pub == 1) url_str = url_str.replace("{code_pub_shot}", "w");
                        else
                            url_str = url_str.replace("{code_pub_shot}", code_pub);
                        url_str = url_str.replace("{code_pub}", code_pub);
                        url_str = url_str.replace("{code_lng}", code_lng);
                        format.applyPattern("yy");
                        url_str = url_str.replace("{YY}",
                                format.format(date));
                        if (cur_pub == 2)
                            format.applyPattern("yyyyMM");
                        else
                            format.applyPattern("yyyyMMdd");
                        url_str = url_str.replace("{YYYYMMDD}",
                                format.format(date));

                        if (funct.load_img(dir, name, url_str)) {
                            Log.i("JWP_image", name
                                    + " - file download complete!");
                            img = 1;
                        } else img = 0;

                    } else {
                        Log.i("JWP_image", name + " - found!");
                        img = 1;
                    }
                }
            }
            return img;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog != null)
                dialog.dismiss();
            Log.e("JWP", "onPostExecute+");
            handler.sendEmptyMessage(1);
        }

        @Override
        protected void onPreExecute() {
            this.dialog = ProgressDialog
                    .show(activity,
                            activity.getResources().getString(
                                    R.string.jornals),
                            activity.getResources().getString(
                                    R.string.dialog_loaing_rss), true, true, new DialogInterface.OnCancelListener() {
                                public void onCancel(DialogInterface pd) {
                                    task.cancel(true);
                                }
                            }
                    );
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
