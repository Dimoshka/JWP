package ua.pp.dimoshka.classes;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.File;
import java.util.Locale;

import ua.pp.dimoshka.jwp.R;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class class_widget_factory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    public static Integer id_lng = 1;

    private SQLiteDatabase database;
    private class_functions funct;
    private SharedPreferences prefs;
    private Cursor cursor;

    class_widget_factory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        funct = new class_functions(context);
        class_sqlite dbOpenHelper = new class_sqlite(context, funct);
        database = dbOpenHelper.openDataBase();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        get_language(Integer.parseInt(prefs.getString("language", "1")));
    }

    private void get_language(int id) {
        try {
            Cursor cursor;
            if (prefs.getBoolean("first_run", true)) {
                cursor = database.rawQuery("SELECT * from language where code_an='"
                        + Locale.getDefault().getLanguage() + "'", null);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    prefs.edit()
                            .putString("language",
                                    cursor.getString(cursor.getColumnIndex("_id")))
                            .apply();
                    id_lng = cursor.getInt(cursor.getColumnIndex("_id"));
                } else {
                    id_lng = 1;
                }
            } else {
                cursor = database.rawQuery("SELECT* from language where _id='" + id + "'", null);
                cursor.moveToFirst();
                if (cursor.getCount() > 0) {
                    id_lng = cursor.getInt(cursor.getColumnIndex("_id"));
                } else {
                    id_lng = 1;
                }
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        cursor.moveToPosition(position);

        RemoteViews view;
        Boolean img = cursor.getInt(cursor.getColumnIndex("img")) != 0;
        if (img) {
            view = new RemoteViews(context.getPackageName(),
                    R.layout.list_items_news_img);
        } else {
            view = new RemoteViews(context.getPackageName(),
                    R.layout.list_items_news_noimg);
        }

        try {
            String titles = cursor.getString(cursor.getColumnIndex("title"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String pubdate = cursor.getString(cursor.getColumnIndex("pubdate"));
            Integer _id = cursor.getInt(cursor.getColumnIndex("_id"));

            view.setTextViewText(R.id.title, titles);
            view.setTextViewText(R.id.text, description);
            view.setTextViewText(R.id.date, pubdate);

            if (img) {
                File imgFile = new File(funct.get_dir_app() + "/img/news/"
                        + _id + ".jpg");
                if (imgFile.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath(), options);
                    view.setImageViewBitmap(R.id.img, bitmap);
                } else {
                    view.setImageViewResource(R.id.img, R.drawable.ic_noimages);
                    ContentValues initialValues = new ContentValues();
                    initialValues.put("img", "0");
                    database.update("news", initialValues, "_id=?",
                            new String[]{_id.toString()});
                }
            }

            Intent clickIntent = new Intent();
            clickIntent.putExtra(class_widget.ITEM_POSITION, position);
            clickIntent.putExtra(class_widget.ITEM_LINK, cursor.getString(cursor.getColumnIndex("link")));
            view.setOnClickFillInIntent(R.id.row_news, clickIntent);
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        cursor = database.rawQuery("select * from news where news.id_lang='" + id_lng + "' order by pubdate desc, news._id asc limit 0, 5", null);
    }

    @Override
    public void onDestroy() {
        database.close();
    }
}