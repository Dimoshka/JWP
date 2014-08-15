package ua.pp.dimoshka.classes;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ua.pp.dimoshka.jwp.R;
import ua.pp.dimoshka.jwp.player;

public class class_open_or_download {

    private Context context;
    private Cursor cursor = null;
    private Cursor cur = null;
    private SQLiteDatabase database;
    private class_functions funct;

    public class_open_or_download(Context context, SQLiteDatabase database, class_functions funct) {
        this.context = context;
        this.database = database;
        this.funct = funct;
    }

    public void dialog_show(long id) {
        try {
            if (funct.ExternalStorageState()) {
                List<String> listItems = new ArrayList<String>();
                CharSequence[] items;

                cursor = database
                        .rawQuery(
                                "select id_type, type.name as name_type, file, files.id_magazine, magazine.id_pub, magazine.favorite from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
                                        + id + "' group by id_type order by files.id_type asc", null
                        );
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        String name;

                        if (i == 0) {
                            if (Boolean.valueOf(cursor.getInt(cursor
                                    .getColumnIndex("favorite")) != 0)) {
                                listItems.add(context.getString(R.string.favorite_remove));
                            } else {
                                listItems.add(context.getString(R.string.favorite_add));
                            }
                        }

                        if (cursor.getInt(cursor
                                .getColumnIndex("id_type")) != 6) {
                            if (cursor.getInt(cursor
                                    .getColumnIndex("file")) == 1) {
                                name = context.getString(R.string.open)
                                        + " "
                                        + cursor.getString(cursor
                                        .getColumnIndex("name_type"));
                            } else {
                                name = context.getString(R.string.download)
                                        + " "
                                        + cursor.getString(cursor
                                        .getColumnIndex("name_type"));
                            }
                        } else {
                            name = context.getString(R.string.player_open)
                                    + " ("
                                    + cursor.getString(cursor
                                    .getColumnIndex("name_type")) + ")";
                        }
                        listItems.add(name);
                        cursor.moveToNext();
                    }

                    items = listItems
                            .toArray(new CharSequence[listItems.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setTitle(context.getString(R.string.choose_the_action));
                    builder.setItems(items,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int item) {
                                    open_or_download(item);
                                }
                            }
                    );
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else
                Toast.makeText(context, R.string.no_sdcard,
                        Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    void open_or_download(int id) {
        try {
            if (cursor.getCount() > 0) {
                if (id == 0) {
                    cursor.moveToPosition(0);
                    ContentValues initialValues = new ContentValues();
                    if (Boolean.valueOf(cursor.getInt(cursor
                            .getColumnIndex("favorite")) != 0)) {
                        initialValues.put("favorite", "0");
                    } else {
                        initialValues.put("favorite", "1");
                    }
                    database.update("magazine", initialValues, "_id=?", new String[]{cursor.getString(cursor.getColumnIndex("id_magazine"))});
                    Intent intent = new Intent("update");
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                    broadcastManager.sendBroadcast(intent);

                } else {
                    cursor.moveToPosition(id - 1);
                    if (funct.ExternalStorageState()) {
                        if (cursor.getInt(cursor.getColumnIndex("id_type")) == 6) {
                            Intent i = new Intent(context, player.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.putExtra("id_magazine", cursor.getInt(cursor
                                    .getColumnIndex("id_magazine")));
                            context.startActivity(i);
                        } else {
                            select_file();
                        }
                    } else
                        Toast.makeText(context, R.string.no_sdcard,
                                Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    void select_file() {
        List<String> listItems = new ArrayList<String>();
        CharSequence[] items;
        cur = database
                .rawQuery(
                        "select files._id, files.name, magazine.name as name_magazine, files.file, files.link, magazine.id_pub, files.title from files left join magazine on files.id_magazine=magazine._id where files.id_magazine='"
                                + cursor.getInt(cursor.getColumnIndex("id_magazine")) + "' and files.id_type='" + cursor.getInt(cursor.getColumnIndex("id_type")) + "' order by files.name asc", null
                );

        if (cur.getCount() == 1) {
            cur.moveToFirst();
            start_open_or_download(cur.getString(cur.getColumnIndex("name")), cur.getString(cur.getColumnIndex("name_magazine")),
                    Boolean.valueOf(cur.getInt(cur.getColumnIndex("file")) != 0),
                    cur.getString(cur.getColumnIndex("link")), Integer.valueOf(cur.getInt(cur.getColumnIndex("id_pub")))
            );
        } else if (cur.getCount() > 1) {
            cur.moveToFirst();
            for (int i = 0; i < cur.getCount(); i++) {
                String name;

                if (cur.getInt(cur
                        .getColumnIndex("file")) == 1) {
                    name = context.getString(R.string.open)
                            + " - "
                            + cur.getString(cur
                            .getColumnIndex("title"));
                } else {
                    name = context.getString(R.string.download)
                            + " - "
                            + cur.getString(cur
                            .getColumnIndex("title"));
                }
                listItems.add(name);
                cur.moveToNext();
            }

            items = listItems.toArray(new CharSequence[listItems.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.choose_the_action));
            builder.setItems(items,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int item) {
                            cur.moveToPosition(item);
                            start_open_or_download(cur.getString(cur.getColumnIndex("name")), cur.getString(cur.getColumnIndex("name_magazine")),
                                    Boolean.valueOf(cur.getInt(cur.getColumnIndex("file")) != 0),
                                    cur.getString(cur.getColumnIndex("link")), Integer.valueOf(cur.getInt(cur.getColumnIndex("id_pub")))
                            );
                        }
                    }
            );
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    void start_open_or_download(String name, String name_magazine, Boolean file_enable,
                                String link, Integer id_pub) {
        Boolean file_enable1 = file_enable;
        try {
            String dir_path_pub;
            if (id_pub.intValue() < 4) dir_path_pub = "/journals/";
            else if (id_pub.intValue() == 4) dir_path_pub = "/books_brochures/";
            else dir_path_pub = "/video/";

            File file = new File(funct.get_dir_app() + "/downloads" + dir_path_pub + name);
            if (!file.exists()) {
                if (file_enable1.booleanValue())
                    funct.update_file_isn(database, name, Integer.valueOf(0));
                file_enable1 = Boolean.FALSE;
            } else {
                if (!file_enable1.booleanValue())
                    funct.update_file_isn(database, name, Integer.valueOf(1));
                file_enable1 = Boolean.TRUE;
            }

            if (file_enable1.booleanValue()) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);

                MimeTypeMap map = MimeTypeMap.getSingleton();
                String ext = MimeTypeMap
                        .getFileExtensionFromUrl(file.getName());
                String type = map.getMimeTypeFromExtension(ext);

                if (type == null)
                    type = "*/*";
                Uri data = Uri.fromFile(file);
                intent.setDataAndType(data, type);
                context.startActivity(intent);

            } else {

                File dir = new File(funct.get_dir_app() + "/downloads" + dir_path_pub);
                if (!dir.isDirectory()) {
                    dir.mkdirs();
                }
                File imgFile = new File(funct.get_dir_app() + "/img" + dir_path_pub + name_magazine + ".jpg");
                Intent i = new Intent(context,
                        service_downloads_files.class);
                i.putExtra("file_url", link);
                i.putExtra("file_putch", file.getAbsolutePath());
                if (imgFile.exists()) i.putExtra("img_putch", imgFile.getAbsolutePath());
                else i.putExtra("img_putch", "");

                Toast.makeText(context,
                        context.getString(R.string.download_task_added),
                        Toast.LENGTH_SHORT).show();
                context.startService(i);
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }
}
