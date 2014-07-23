package ua.pp.dimoshka.classes;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.pp.dimoshka.jwp.R;
import ua.pp.dimoshka.jwp.main;

public class class_rss_news {
    static final String URL_FEED_NEWS = "http://www.jw.org/%s/rss/LatestNewsList/feed.xml";
    static final String URL_FEED_NEW_IN_SITE = "http://www.jw.org/%s/rss/WhatsNewWebArticles/feed.xml";

    private SQLiteDatabase database;

    public class_functions funct;
    private Context context;
    private Handler handler;
    private boolean show_dialog;
    public SharedPreferences prefs;
    private AsyncTask task;

    public class_rss_news(Context context, Handler handler,
                          SQLiteDatabase database, class_functions funct, boolean show_dialog) {
        this.context = context;
        this.handler = handler;
        this.database = database;
        this.funct = funct;
        this.show_dialog = show_dialog;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void get_all_feeds() {
        try {
            Log.d("JWP", "get_all_feeds");
            task = new ReadFeedTask().execute();
        } catch (Exception ex) {
            funct.send_bug_report(ex);
        }
    }

    class ReadFeedTask extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog;
        List<class_rss_item> rss_list = null;

        @Override
        protected Void doInBackground(Void... paramArrayOfVoid) {
            try {
                class_rss_provider rssfeedprovider = new class_rss_provider(context, funct);
                String feed = String.format(URL_FEED_NEW_IN_SITE, main.ln_prefix);
                Log.d("JWP-news", feed);
                this.rss_list = rssfeedprovider.parse(feed);

                for (class_rss_item aRss_list : rss_list) {
                    if (isCancelled()) {
                        int currentapiVersion = Build.VERSION.SDK_INT;
                        if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            Log.d("JWP", "isCancelled+");
                            if (dialog != null)
                                dialog.dismiss();
                            Log.d("JWP", "onPostExecute+");
                            handler.sendEmptyMessage(1);
                        }
                        break;
                    }
                    class_rss_item rss_item = aRss_list;
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

                    description = funct.stripHtml(description);
                    description = description.trim();
                    long id_news;
                    ContentValues init = new ContentValues();
                    init.put("id_lang", main.id_lng);
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
            if (show_dialog) {
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
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

    }

}
