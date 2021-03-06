package ua.pp.dimoshka.classes;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ua.pp.dimoshka.jwp.R;

public class class_video {
    private static final String URL_SITE = "http://www.jw.org/";
    private SQLiteDatabase database;
    private class_functions funct;
    private Context context;
    private Cursor cursor = null;
    private AsyncTask task = null;
    private SharedPreferences prefs;
    private ArrayList<Integer> id_type = new ArrayList<Integer>();
    private ArrayList<String> code_type = new ArrayList<String>();
    private ArrayList<String> name_type = new ArrayList<String>();

    public class_video(Context context, SQLiteDatabase database, class_functions funct) {
        this.context = context;
        this.database = database;
        this.funct = funct;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        get_publication();
    }

    public void verify_all() {
        task = new load_from_site().execute();
    }

    final void get_publication() {
        try {
            Cursor cursor_type = database.query("type", new String[]{"_id",
                    "name", "code"}, "_id BETWEEN '11' and '15'", null, null, null, "_id");
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

    class load_from_site extends AsyncTask<Void, Integer, Void> {
        load_from_site() {
        }

        @Override
        protected Void doInBackground(Void[] paramArrayOfVoid) {
            try {
                DateFormat dat_format = new SimpleDateFormat("yyyy-MM-dd");
                Date date_now = new Date();

                if (funct.ExternalStorageState()) {
                    Document doc = Jsoup.connect(URL_SITE + funct.get_video_prefix() + "?sortBy=1").get();
                    Elements pages = doc.getElementsByClass("pageNum");

                    //Elements pages_a = pages.get(0).getElementsByTag("a");
                    ArrayList<String> pages_list = new ArrayList<String>();
                    pages_list.add(URL_SITE + funct.get_video_prefix());

                    for (Iterator<Element> iterator = pages.iterator(); iterator.hasNext(); ) {
                        Element link = iterator.next();
                        pages_list.add(URL_SITE + link.attr("href"));
                        //Log.e("VIDEO", link.attr("href") + " " + link.text());
                    }


                    for (int i = 0; i < pages_list.size(); i++) {
                        if (isCancelled()) {
                            int currentapiVersion = Build.VERSION.SDK_INT;
                            if (currentapiVersion >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                                Log.d("JWP", "isCancelled+");
                                Log.d("JWP", "onPostExecute+");
                                funct.send_to_local_brodcast("loading", new HashMap<String, Integer>() {{
                                    put("page", 3);
                                    put("status", 0);
                                }});


                            }
                            break;
                        }

                        if (i != 0) {
                            doc = Jsoup.connect(pages_list.get(i)).get();
                        }
                        Elements publications = doc.getElementsByClass("synopsis");


                        for (Iterator<Element> iterator = publications.iterator(); iterator.hasNext(); ) {
                            Element publ = iterator.next();
                            String img_link = null;
                            String name = null;
                            Elements img_el = publ.getElementsByClass("jsRespImg");
                            if (img_el.size() > 0) {
                                img_link = img_el.get(0).attr("data-img-size-xs").trim();
                                //Log.e("Video", img_link);
                                String[] a = img_link.split("/");
                                name = a[a.length - 1].replace("_xs.jpg", "").toString() + "_" + funct.get_code_lng();
                            } else continue;

                            Cursor cur = database.rawQuery(
                                    "select _id, img from magazine where `name` = '" + name + "'", null);
                            long id_magazine = -1;
                            Integer img = Integer.valueOf(img(name, img_link));

                            if (cur.getCount() > 0) {
                                cur.moveToFirst();
                                id_magazine = cur.getLong(cur
                                        .getColumnIndex("_id"));
                                if (img.intValue() != cur.getInt(cur
                                        .getColumnIndex("img"))) {
                                    ContentValues init = new ContentValues();
                                    init.put("img", img);
                                    Log.d("JWP_img", "update img to " + img.toString());
                                    //database.update("magazine", init, "_id=?",
                                    //        new String[]{String.valueOf(id_magazine)});
                                }
                            } else {
                                String title = null;
                                String descript = null;

                                Elements publicationDesc = publ.getElementsByClass("syn-body");
                                if (publicationDesc.size() > 0) {
                                    Elements h3 = publicationDesc.get(0).getElementsByTag("h3");
                                    if (h3.size() > 0) {
                                        title = funct.stripHtml(h3.text());
                                    } else continue;

                                    Elements desc = publicationDesc.get(0).getElementsByClass("desc");
                                    if (desc.size() > 0) {
                                        descript = funct.stripHtml(desc.text());
                                    }
                                } else continue;

                                ContentValues init1 = new ContentValues();
                                init1.put("name", name);
                                init1.put("title", title);
                                init1.put("id_pub", Integer.valueOf(5));
                                init1.put("id_lang", funct.get_id_lng());
                                init1.put("img", img);
                                init1.put("date", dat_format.format(date_now));
                                init1.put("link_img", img_link);
                                id_magazine = database.insert("magazine", null, init1);
                                //id_magazine = database.insertWithOnConflict("magazine", null, init1, SQLiteDatabase.CONFLICT_IGNORE);
                            }

                            //Добавление файлов
                            try {
                                Elements jsCoverDoc = publ.getElementsByClass("jsCoverDoc");
                                if (jsCoverDoc.size() > 0) {
                                    for (int a = 0; a < jsCoverDoc.size(); a++) {


                                        Map<String, ArrayList<Map<String, String>>> hm = funct.get_json_files((URL_SITE + jsCoverDoc.get(a).attr("data-jsonurl").trim()).replace("//apps", "/apps"));
                                        if (hm.size() > 0) {
                                            for (String key : hm.keySet()) {
                                                ArrayList<Map<String, String>> hm_format_arr = hm.get(key);
                                                for (int f = 0; f < hm_format_arr.size(); f++) {
                                                    Map<String, String> hm_format = hm_format_arr.get(f);
                                                    int id = name_type.indexOf(key);
                                                    if (id > -1) {
                                                        if (id_magazine > -1) {
                                                            ContentValues init = new ContentValues();
                                                            init.put("id_magazine", Long.valueOf(id_magazine));
                                                            init.put("id_type", id_type.get(id));
                                                            init.put("name", hm_format.get("name"));
                                                            init.put("link", hm_format.get("url"));
                                                            init.put("pubdate", dat_format.format(date_now));
                                                            init.put("title", hm_format.get("title") + " " + hm_format.get("label"));
                                                            init.put("file", Integer.valueOf(0));
                                                            //Log.e("Pub", init.toString());
                                                            database.insertWithOnConflict("files", null, init, SQLiteDatabase.CONFLICT_IGNORE);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else continue;
                            } catch (Exception ex) {
                                funct.send_bug_report(ex);
                                continue;
                            }
                        }
                        publishProgress(Integer.valueOf(((i + 1) * 100) / pages_list.size()));
                    }
                }
            } catch (SocketTimeoutException e) {
                //funct.send_bug_report(e);
            } catch (UnknownHostException e) {
                //funct.send_bug_report(e);
            } catch (Exception e) {
                funct.send_bug_report(e);
            }
            return null;
        }


        int img(String name, String link_img) {
            int img = 0;
            if (prefs.getBoolean("downloads_img", true)) {
                if (funct.ExternalStorageState()) {
                    File dir = new File(funct.get_dir_app() + "/img/video/");
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
        protected void onProgressUpdate(Integer[] progUpdate) {
            if (progUpdate[0].intValue() >= 0) {  // change the 10000 to whatever
                Toast.makeText(context, context.getResources().getString(
                                R.string.video) + " - " + context.getResources().getString(
                                R.string.dialog_loaing_site) + " " + progUpdate[0] + "%", Toast.LENGTH_SHORT
                ).show();
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            try {
                if (cursor != null && !cursor.isClosed()) {
                    cursor.close();
                }
            } catch (final IllegalArgumentException e) {
                // Handle or log or ignore
            } catch (final Exception e) {
                // Handle or log or ignore
            } finally {
                Log.d("JWP", "onPostExecute+");
                funct.send_to_local_brodcast("loading", new HashMap<String, Integer>() {{
                    put("page", 3);
                    put("status", 1);
                }});
            }
        }

        @Override
        protected void onPreExecute() {
            funct.send_to_local_brodcast("loading", new HashMap<String, Integer>() {{
                put("page", 3);
                put("status", 2);
            }});

            Toast.makeText(context, context.getResources().getString(
                            R.string.video) + " - " + context.getResources().getString(
                            R.string.dialog_loaing_site), Toast.LENGTH_SHORT
            ).show();

        }
    }
}
