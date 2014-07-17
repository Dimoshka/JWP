package com.dimoshka.ua.classes;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class class_rss_news {
    static final String URL_FEED_NEWS = "http://www.jw.org/%s/rss/LatestNewsList/feed.xml";
    static final String URL_FEED_NEW_IN_SITE = "http://www.jw.org/%s/rss/WhatsNewWebArticles/feed.xml";

    private class_rss_provider rssfeedprovider;
    private SQLiteDatabase database;

    public class_functions funct;
    private Context context;
    private Handler handler;
    private Integer id_ln = 0;

    private String ln_prefix = "en/news";
    public SharedPreferences prefs;
    private AsyncTask task;

    public class_rss_news(Context context, int id_lang, Handler handler,
                          SQLiteDatabase database, class_functions funct) {
        this.context = context;
        this.handler = handler;
        this.database = database;
        this.funct = funct;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        get_language(id_lang);
    }

    public Integer get_language(int id) {
        Cursor cursor = funct.get_language(database, id);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            id_ln = cursor.getInt(cursor.getColumnIndex("_id"));
            ln_prefix = cursor.getString(cursor.getColumnIndex("news_rss"));
        } else {
            id_ln = 1;
            ln_prefix = "en/news";
        }
        return id_ln;
    }

    public void get_all_feeds() {
        try {
            task = new ReadFeedTask().execute();
        } catch (Exception e) {
            Log.d("JWP_" + getClass().getName(), e.toString());
        }
    }

    class ReadFeedTask extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog;
        List<class_rss_item> rss_list = null;

        @Override
        protected Void doInBackground(Void... paramArrayOfVoid) {
            try {
                rssfeedprovider = new class_rss_provider(context, funct);
                String feed = String.format(URL_FEED_NEW_IN_SITE, ln_prefix);
                Log.d("JWP-news", feed);
                this.rss_list = rssfeedprovider.parse(feed);

                for (int i = 0; i < rss_list.size(); i++) {
                    if (isCancelled()) {
                        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                        if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            Log.d("JWP", "isCancelled+");
                            if (dialog != null)
                                dialog.dismiss();
                            Log.d("JWP", "onPostExecute+");
                            handler.sendEmptyMessage(1);
                        }
                        break;
                    }
                    class_rss_item rss_item = rss_list.get(i);
                    String title = rss_item.getTitle();
                    title = title.trim();
                    String link = rss_item.getLink();
                    String description = rss_item.getDescription();
                    String pubdate = rss_item.getPubDate();

                    DateFormat format = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date date = funct.get_string_to_date(pubdate,
                            "EEE, dd MMM yyyy HH:mm:ss Z");

                    String reg_text = "<![CDATA[(.*)]]>";
                    String reg_img = "<img(.*)/>";
                    String reg_img_url = "src=\"(.*\\s*.jpg)\"";
                    String link_img = "";

                    Pattern p = Pattern.compile(reg_text);
                    Matcher mat = p.matcher(description);

                    if (mat.find()) {
                        description = mat.group(1);
                    }

                    Pattern p2 = Pattern.compile(reg_img_url);
                    Matcher mat2 = p2.matcher(description);
                    if (mat2.find()) {
                        link_img = mat2.group(1);
                    }

                    Pattern p1 = Pattern.compile(reg_img);
                    Matcher mat1 = p1.matcher(description);
                    if (mat1.find()) {
                        description = description.replace(mat1.group(0), "");
                    }

                    description = description.trim();
                    description = funct.stripHtml(description);
                    long id_news = 0;
                    ContentValues init = new ContentValues();
                    init.put("id_lang", id_ln);
                    init.put("title", title);
                    init.put("link", link);
                    init.put("link_img", link_img);
                    init.put("description", description);
                    init.put("pubdate", format.format(date));
                    id_news = database.insertWithOnConflict("news", null, init,
                            SQLiteDatabase.CONFLICT_IGNORE);
                    if (id_news > -1) {
                        int img = img(id_news, link_img);
                        ContentValues init2 = new ContentValues();
                        init2.put("img", img);
                        String[] args = {String.valueOf(id_news)};
                        database.update("news", init2, "_id=?", args);
                    }
                }

            } catch (Exception e) {
                funct.send_bug_report(e);
            }
            return null;
        }

        protected int img(long _id, String link_img) {
            int img = 0;
            if (prefs.getBoolean("downloads_img", true)) {
                if (funct.ExternalStorageState()) {
                    File dir = new File(funct.get_dir_app() + "/img/news/");
                    if (!dir.isDirectory()) {
                        dir.mkdirs();
                    }
                    String name = _id + "";
                    if (link_img.length() > 0) {
                        File imgFile = new File(dir.getAbsolutePath() + name
                                + ".jpg");
                        if (!imgFile.exists()) {
                            Log.i("JWP_image", name + " - no found!");
                            if (funct.load_img(dir.getAbsolutePath(), name, link_img)) {
                                Log.i("JWP_image", name
                                        + " - file download complete!");
                                img = 1;
                            }
                        } else {
                            img = 1;
                        }
                    } else img = 0;
                }
            }
            return img;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog != null)
                dialog.dismiss();
            Log.d("JWP", "onPostExecute+");
            handler.sendEmptyMessage(1);
        }

        @Override
        protected void onPreExecute() {
            this.dialog = ProgressDialog
                    .show(context,
                            context.getResources().getString(
                                    R.string.news),
                            context.getResources().getString(
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
