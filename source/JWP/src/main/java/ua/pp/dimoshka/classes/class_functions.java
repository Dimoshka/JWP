package ua.pp.dimoshka.classes;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.Html;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ua.pp.dimoshka.jwp.R;

public class class_functions {

    private Context context;
    private Integer id_lng = Integer.valueOf(1);
    private String news_prefix = "en/news";
    private String books_brochures_prefix = "en/publications/";
    private String video_prefix = "en/videos/";
    private String code_lng = "E";

    public Integer get_id_lng() {
        return id_lng;
    }

    public String get_news_prefix() {
        return news_prefix;
    }

    public String get_books_brochures_prefix() {
        return books_brochures_prefix;
    }

    public String get_video_prefix() {
        return video_prefix;
    }

    public String get_code_lng() {
        return code_lng;
    }


    public class_functions(Context context) {
        this.context = context;
    }

    public boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            }
        } catch (Exception e) {
            Log.d("JWP_" + getClass().getName(), e.toString());
        }
        return false;
    }

    public boolean ExternalStorageState() {
        try {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return false;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }


    public Date get_jwp_journals_rss_date(String name, String code_pub,
                                          String code_lng) {
        String date_str = name;
        date_str = date_str.replace(code_pub + "_", "");
        date_str = date_str.replace(code_lng + "_", "");
        date_str = date_str.replace(".", "");
        if (date_str.length() > 8) {
            date_str = date_str.substring(0, date_str.length() - 3);
        }
        date_str = date_str.replace("_", "");
        if (date_str.length() == 6) {
            date_str += "01";
        }
        return get_string_to_date(date_str, "yyyyMMdd");
    }


    public Date get_string_to_date(String date_str, String format_str) {
        SimpleDateFormat format = new SimpleDateFormat(format_str, Locale.US);
        format.setLenient(true);
        Date date;
        try {
            date = format.parse(date_str);
        } catch (java.text.ParseException e) {
            Log.d("JWP_date", e.toString() + " - " + date_str + " - " + format_str);
            return null;
        }
        return date;
    }

    public String get_dir_app() {
        return Environment.getExternalStorageDirectory() + "/"
                + context.getResources().getString(R.string.app_dir);
    }

    public String getMonth(int month) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.getDefault());
        Calendar localCalendar = Calendar.getInstance();
        localCalendar.set(Calendar.MONTH, month);
        String m = sdf.format(localCalendar.getTime());
        m = m.replaceFirst(m.substring(0, 1),
                m.substring(0, 1).toUpperCase(Locale.getDefault()));
        return m;
    }

    public void update_file_isn(SQLiteDatabase database, String name, Integer file) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("file", file.toString());
        database.update("files", initialValues, "name=?", new String[]{name});
    }

    public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    public void send_bug_report(Exception ex) {
        try {
            Log.e("EERROORR", "FN_class", ex);
            //BugSenseHandler.addCrashExtraData("StackTrace", message);
            BugSenseHandler.sendException(ex);
            //BugSenseHandler.sendExceptionMessage("level", message, ex);
        } catch (Exception e) {
            ex.printStackTrace();
        }
    }

    public void get_language(SQLiteDatabase database, SharedPreferences prefs) {
        try {
            Cursor cursor;
            int id = Integer.parseInt(prefs.getString("language", "1"));

            if (prefs.getBoolean("first_run", true)) {
                cursor = database.rawQuery("SELECT * from language where code_an='"
                        + Locale.getDefault().getLanguage() + "'", null);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    prefs.edit()
                            .putString("language",
                                    cursor.getString(cursor.getColumnIndex("_id")))
                            .apply();
                    id_lng = Integer.valueOf(cursor.getInt(cursor.getColumnIndex("_id")));
                    news_prefix = cursor.getString(cursor.getColumnIndex("news_rss"));
                    code_lng = cursor.getString(cursor.getColumnIndex("code"));
                    books_brochures_prefix = cursor.getString(cursor.getColumnIndex("books_brochures_link"));
                    video_prefix = cursor.getString(cursor.getColumnIndex("video_link"));

                } else {
                    id_lng = Integer.valueOf(1);
                    news_prefix = "en/news";
                    books_brochures_prefix = "en/publications/";
                    video_prefix = "en/videos/";
                    code_lng = "E";
                }
            } else {
                cursor = database.rawQuery("SELECT* from language where _id='" + id + "'", null);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    id_lng = Integer.valueOf(cursor.getInt(cursor.getColumnIndex("_id")));
                    news_prefix = cursor.getString(cursor.getColumnIndex("news_rss"));
                    code_lng = cursor.getString(cursor.getColumnIndex("code"));
                    books_brochures_prefix = cursor.getString(cursor.getColumnIndex("books_brochures_link"));
                    video_prefix = cursor.getString(cursor.getColumnIndex("video_link"));
                } else {
                    id_lng = Integer.valueOf(1);
                    news_prefix = "en/news";
                    books_brochures_prefix = "en/publications";
                    video_prefix = "en/videos/";
                    code_lng = "E";
                }
            }
        } catch (Exception e) {
            send_bug_report(e);
        }
    }


    public boolean load_img(String dir, String name, String link_img) {
        if (isNetworkAvailable()) {
            try {
                URL obj = new URL(link_img);
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setReadTimeout(5000);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                //conn.addRequestProperty("Referer", "google.com");
                try {
                    //Log.e("Response Code ... ", conn.getResponseCode() + "");
                    //Log.e("Response Code ... ", link_img);
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = conn.getInputStream();
                        BufferedInputStream bis = new BufferedInputStream(is);
                        ByteArrayBuffer baf = new ByteArrayBuffer(
                                5000);
                        int current = 0;
                        while ((current = bis.read()) != -1) {
                            baf.append((byte) current);
                        }
                        File file = new File(dir, name + ".jpg");
                        FileOutputStream fos = new FileOutputStream(
                                file);
                        fos.write(baf.toByteArray());
                        fos.flush();
                        fos.close();
                        return true;
                    } else return false;
                } catch (SocketTimeoutException e) {
                    return false;
                } catch (UnknownHostException e) {
                    return false;
                } catch (FileNotFoundException e) {
                    return false;
                } catch (Exception e) {
                    send_bug_report(e);
                    return false;
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                return false;
            }
        } else return false;
    }

}
