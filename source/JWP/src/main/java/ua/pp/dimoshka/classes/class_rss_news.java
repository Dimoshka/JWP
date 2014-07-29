package ua.pp.dimoshka.classes;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ua.pp.dimoshka.jwp.R;
import ua.pp.dimoshka.jwp.main;

public class class_rss_news {
    static final String URL_FEED_NEWS = "http://www.jw.org/%s/rss/LatestNewsList/feed.xml";
    private static final String URL_FEED_NEW_IN_SITE = "http://www.jw.org/%s/rss/WhatsNewWebArticles/feed.xml";

    private SQLiteDatabase database;

    private class_functions funct;
    private Context context;
    private Handler handler = null;
    private boolean is_activity = false;
    private SharedPreferences prefs;
    private AsyncTask task = null;
    private int appWidgetId = 0;

    public class_rss_news(Context context, SQLiteDatabase database, class_functions funct) {
        this.context = context;
        this.database = database;
        this.funct = funct;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }


    public void get_all_feeds_activity(Handler handler) {
        try {
            Log.d("JWP", "get_all_feeds_activity");
            this.handler = handler;
            is_activity = true;
            task = new ReadFeedTask().execute();
        } catch (Exception ex) {
            funct.send_bug_report(ex);
        }
    }

    public void get_all_feeds_widget(int appWidgetId) {
        try {
            Log.d("JWP", "get_all_feeds_widget");
            is_activity = false;
            this.appWidgetId = appWidgetId;
            task = new ReadFeedTask().execute();
        } catch (Exception ex) {
            funct.send_bug_report(ex);
        }
    }

    private void update_widget() {
        try {
            Log.d("WIDGET", "start update afte load rss");
            Intent updIntent = new Intent(context, class_widget.class);
            updIntent.setAction(class_widget.ACTION_ON_UPDATEOK);
            updIntent.putExtra(class_widget.IDWIDGET, appWidgetId);
            PendingIntent updPIntent = PendingIntent.getBroadcast(context,
                    appWidgetId, updIntent, 0);
            updPIntent.send(context, 0, updIntent);
        } catch (Exception ex) {
            funct.send_bug_report(ex);
        }

    }

    class ReadFeedTask extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog = null;
        List<class_rss_item> rss_list = null;

        ReadFeedTask() {
        }

        @Override
        protected Void doInBackground(Void[] paramArrayOfVoid) {
            try {
                class_rss_provider rssfeedprovider = new class_rss_provider(context, funct);
                String feed = String.format(URL_FEED_NEW_IN_SITE, main.ln_prefix);
                Log.d("JWP-news", feed);
                this.rss_list = rssfeedprovider.parse(feed);
                ArrayList<ArrayList> items_news = new ArrayList();

                for (Iterator<class_rss_item> iterator = rss_list.iterator(); iterator.hasNext(); ) {
                    class_rss_item aRss_list = iterator.next();

                    if (isCancelled()) {
                        int currentapiVersion = Build.VERSION.SDK_INT;
                        if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                            Log.d("JWP", "isCancelled+");
                            if (dialog != null)
                                dialog.dismiss();
                            Log.d("JWP", "onPostExecute+");
                            if (is_activity) {
                                handler.sendEmptyMessage(0);
                            }
                        }
                        break;
                    }
                    @SuppressWarnings("UnnecessaryLocalVariable") class_rss_item rss_item = aRss_list;
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

                    ArrayList<String> items = new ArrayList();
                    items.add(main.id_lng.toString());
                    items.add(title);
                    items.add(link);
                    items.add(link_img);
                    items.add(description);
                    items.add(format.format(date));
                    items_news.add(items);
                }

                for (int i = items_news.size(); i > 0; i--) {
                    ArrayList<String> items = items_news.get(i - 1);
                    long id_news;
                    ContentValues init = new ContentValues();
                    init.put("id_lang", items.get(0));
                    init.put("title", items.get(1));
                    init.put("link", items.get(2));
                    init.put("link_img", items.get(3));
                    init.put("description", items.get(4));
                    init.put("pubdate", items.get(5));
                    id_news = database.insertWithOnConflict("news", null, init,
                            SQLiteDatabase.CONFLICT_IGNORE);
                    if (id_news > -1) {
                        int img = img(id_news, items.get(3));
                        ContentValues init2 = new ContentValues();
                        init2.put("img", Integer.valueOf(img));
                        String[] args = {String.valueOf(id_news)};
                        database.update("news", init2, "_id=?", args);
                    }
                }


            } catch (Exception e) {
                funct.send_bug_report(e);
            }
            return null;
        }

        int img(long _id, String link_img) {
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
            try {
                if ((dialog != null) && dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                //dialog = null;
                Log.d("JWP", "onPostExecute+");
                if (is_activity) {
                    handler.sendEmptyMessage(1);
                } else {
                    update_widget();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            if (is_activity) {
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
