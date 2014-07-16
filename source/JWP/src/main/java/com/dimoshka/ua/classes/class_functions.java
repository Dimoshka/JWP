package com.dimoshka.ua.classes;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.dimoshka.ua.jwp.R;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class class_functions {
    private SharedPreferences prefs;
    private Context context;

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
            Log.e("JWP_" + getClass().getName(), e.toString());
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


    public Date get_jwp_jornals_rss_date(String name, String code_pub,
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
            Log.e("JWP_date", e.toString() + " - " + date_str + " - " + format_str);
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

    public Cursor get_language(SQLiteDatabase database, Integer id) {
        Cursor cursor;
        if (id == 0) {
            cursor = database.rawQuery("SELECT * from language where code_an='"
                    + Locale.getDefault().getLanguage() + "'", null);
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                prefs = PreferenceManager.getDefaultSharedPreferences(context);
                prefs.edit()
                        .putString("language",
                                cursor.getString(cursor.getColumnIndex("_id")))
                        .commit();
            }
        } else {
            cursor = database.rawQuery("SELECT* from language where _id='" + id
                    + "'", null);
        }
        return cursor;
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
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            String message = "";
            if (stackTraceElements.length >= 3) {
                StackTraceElement element = stackTraceElements[2];
                String className = element.getClassName();
                String methodName = element.getMethodName();
                message = className + ": " + methodName;
            }

            Log.e(context.getString(R.string.app_name_shot) + " - error: " + message,
                    ex.toString());
            BugSenseHandler.addCrashExtraData("class_name", message);
            BugSenseHandler.sendException(ex);
            BugSenseHandler.sendExceptionMessage("level", message, ex);
        } catch (Exception e) {
            Log.e("error: functionn",
                    ex.toString());
        }
    }

    public boolean load_img(String dir, String name, String link_img) {
        try {
            if (isNetworkAvailable()) {
                URL url = new URL(link_img);
                File file = new File(dir, name + ".jpg");
                URLConnection ucon = url.openConnection();
                InputStream is = ucon.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(
                        is);
                ByteArrayBuffer baf = new ByteArrayBuffer(
                        5000);
                int current = 0;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }
                FileOutputStream fos = new FileOutputStream(
                        file);
                fos.write(baf.toByteArray());
                fos.flush();
                fos.close();
                return true;
            } else return false;
        } catch (FileNotFoundException e) {
            Log.e("JWP", "Not file - " + link_img);
            return false;
        } catch (Exception e) {
            send_bug_report(e);
            return false;
        }
    }

}
