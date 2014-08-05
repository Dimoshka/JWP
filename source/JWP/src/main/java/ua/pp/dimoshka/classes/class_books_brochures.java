package ua.pp.dimoshka.classes;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ua.pp.dimoshka.jwp.R;
import ua.pp.dimoshka.jwp.main;

public class class_books_brochures {
    private SQLiteDatabase database;
    private class_functions funct;
    private Context context;
    private Cursor cursor = null;
    private Handler handler;
    private AsyncTask task = null;
    private SharedPreferences prefs;

    private ArrayList<Integer> id_type = new ArrayList<Integer>();
    private ArrayList<String> code_type = new ArrayList<String>();
    private ArrayList<String> name_type = new ArrayList<String>();


    private static final String URL_SITE = "http://www.jw.org/";

    public class_books_brochures(Context context, Handler handler,
                                 SQLiteDatabase database, class_functions funct) {
        this.context = context;
        this.handler = handler;
        this.database = database;
        this.funct = funct;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        get_publication();
    }

    public void verify_all() {
        task = new verify_img().execute();
    }

    void get_publication() {
        try {
            Cursor cursor_type = database.query("type", new String[]{"_id",
                    "name", "code"}, null, null, null, null, "_id");
            cursor_type.moveToFirst();

            for (int i = 0; i < cursor_type.getCount(); i++) {
                id_type.add(Integer.valueOf(cursor_type.getInt(cursor_type
                        .getColumnIndex("_id"))));
                name_type.add(cursor_type.getString(cursor_type
                        .getColumnIndex("name")));
                code_type.add(cursor_type.getString(cursor_type
                        .getColumnIndex("code")));
                cursor_type.moveToNext();
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    class verify_img extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog = null;

        verify_img() {
        }

        @Override
        protected Void doInBackground(Void[] paramArrayOfVoid) {
            try {
                DateFormat dat_format = new SimpleDateFormat("yyyy-MM-dd");
                Date date_now = new Date();

                if (funct.ExternalStorageState()) {
                    Document doc = Jsoup.connect(URL_SITE + main.books_brochures_prefix + "?sortBy=1").get();
                    Elements pages = doc.getElementsByClass("pageNum");

                    //Elements pages_a = pages.get(0).getElementsByTag("a");
                    ArrayList<String> pages_list = new ArrayList();
                    pages_list.add(URL_SITE + main.books_brochures_prefix);

                    for (Element link : pages) {
                        pages_list.add(URL_SITE + link.attr("href"));
                        //Log.e("BOOKS", link.attr("href") + " " + link.text());
                    }


                    for (int i = 0; i < pages_list.size(); i++) {

                        if (isCancelled()) {
                            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                            if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                Log.d("JWP", "isCancelled+");
                                if (dialog != null)
                                    dialog.dismiss();
                                Log.d("JWP", "onPostExecute+");
                                handler.sendEmptyMessage(0);
                            }
                            break;
                        }


                        if (i != 0) {
                            doc = Jsoup.connect(pages_list.get(i)).get();
                        }
                        Elements publications = doc.getElementsByClass("synopsis");


                        for (Element publ : publications) {

                            String img_link = null;
                            String title = null;
                            String name = null;

                            Elements img_el = publ.getElementsByClass("hideObj");
                            if (img_el.size() > 0) {
                                img_link = URL_SITE + img_el.get(0).attr("data-src");
                                String[] a = img_link.split("/");
                                name = a[a.length - 1].replace("_xs.jpg", "").toString();

                            } else continue;
                            Elements publicationDesc = publ.getElementsByClass("publicationDesc");
                            if (publicationDesc.size() > 0) {
                                Elements h3 = publicationDesc.get(0).getElementsByTag("h3");
                                if (h3.size() > 0) {
                                    title = funct.stripHtml(h3.text());
                                } else continue;
                            } else continue;


                            Cursor cur = database.rawQuery(
                                    "select _id, img from magazine where `name` = '" + name + "'", null);
                            long id_magazine = -1;
                            Integer img = img(name, img_link);


                            if (cur.getCount() > 0) {
                                cur.moveToFirst();
                                id_magazine = cur.getLong(cur
                                        .getColumnIndex("_id"));
                                if (img.intValue() != cur.getInt(cur
                                        .getColumnIndex("img"))) {
                                    ContentValues init = new ContentValues();
                                    init.put("img", img);
                                    Log.d("JWP_img", "update img to " + img.toString());
                                    database.update("magazine", init, "_id=?",
                                            new String[]{String.valueOf(id_magazine)});
                                }
                            } else {
                                ContentValues init1 = new ContentValues();
                                init1.put("name", name);
                                init1.put("title", title);
                                init1.put("id_pub", 4);
                                init1.put("id_lang", main.id_lng);
                                init1.put("img", img);
                                init1.put("date", dat_format.format(date_now));
                                init1.put("link_img", img_link);
                                id_magazine = database.insert("magazine", null, init1);
                                //id_magazine = database.insertWithOnConflict("magazine", null, init1, SQLiteDatabase.CONFLICT_IGNORE);

                            }


                            Elements downloadLinks = publ.getElementsByClass("downloadLinks");
                            if (downloadLinks.size() > 0) {
                                Elements jsToolTip = downloadLinks.get(0).getElementsByClass("jsToolTip").addClass("fileLinks");
                                if (jsToolTip.size() > 0) {
                                    for (int a = 0; a < jsToolTip.size(); a++) {
                                        Elements ahref = jsToolTip.get(a).getElementsByTag("a");
                                        if (ahref.size() > 0) {
                                            for (int b = 0; b < ahref.size(); b++) {
                                                //Log.e("Pub", id_magazine + " - " + ahref.get(b).text().trim() + " " + (URL_SITE + ahref.get(b).attr("href")).replace("//apps", "/apps"));
                                                int id = name_type.indexOf(ahref.get(b).text().trim());
                                                if (id > -1) {
                                                    if (id_type.get(id) != 3 && id_magazine > -1) {
                                                        ContentValues init = new ContentValues();
                                                        init.put("id_magazine", id_magazine);
                                                        init.put("id_type", id_type.get(id));
                                                        init.put("name", name + "." + code_type.get(id));
                                                        init.put("link", (URL_SITE + ahref.get(b).attr("href")).replace("//apps", "/apps"));
                                                        init.put("pubdate", dat_format.format(date_now));
                                                        init.put("title", "");
                                                        init.put("file", 0);
                                                        database.insertWithOnConflict("files", null, init, SQLiteDatabase.CONFLICT_IGNORE);
                                                        //Log.e("Pub", init.toString());
                                                    }
                                                } else continue;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                funct.send_bug_report(e);
            }
            return null;
        }


        int img(String name, String link_img) {
            int img = 0;
            if (prefs.getBoolean("downloads_img", true)) {
                if (funct.ExternalStorageState()) {
                    File dir = new File(funct.get_dir_app() + "/img/books_brochures/");
                    if (!dir.isDirectory()) {
                        dir.mkdirs();
                    }
                    if (link_img.length() > 0) {
                        File imgFile = new File(dir.getAbsolutePath() + name
                                + ".jpg");
                        if (!imgFile.exists()) {
                            Log.d("JWP_image", name + " - no found!");
                            if (funct.load_img(dir.getAbsolutePath(), name, link_img)) {
                                Log.d("JWP_image", name
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
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                //dialog = null;
                //cursor = null;
                Log.d("JWP", "onPostExecute+");
                handler.sendEmptyMessage(1);
            }
        }

        @Override
        protected void onPreExecute() {
            this.dialog = ProgressDialog
                    .show(context,
                            context.getResources().getString(
                                    R.string.books_brochures),
                            context.getResources().getString(
                                    R.string.dialog_loaing_img), true, true, new DialogInterface.OnCancelListener() {
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
