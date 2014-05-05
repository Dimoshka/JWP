package com.dimoshka.ua.classes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.dimoshka.ua.jwp.R;
import com.dimoshka.ua.jwp.main;
import com.dimoshka.ua.jwp.player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by designers on 23.05.13.
 */
public class class_open_or_download {

    private Activity activity;
    private Cursor cur_files;
    private SQLiteDatabase database;

    public class_open_or_download(Activity activity, SQLiteDatabase database) {
        this.activity = activity;
        this.database = database;
    }

    public void dialog_show(String _id) {
        try {
            if (main.funct.ExternalStorageState() == true) {
                List<String> listItems = new ArrayList<String>();
                CharSequence[] items = null;

                cur_files = database
                        .rawQuery(
                                "select id_type, file, type.name as name_type, files.name, link, files.id_magazine from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
                                        + _id + "' group by id_type", null);
                if (cur_files.getCount() > 0) {
                    cur_files.moveToFirst();
                    for (int i = 0; i < cur_files.getCount(); i++) {
                        String name = null;

                        if (cur_files.getInt(cur_files
                                .getColumnIndex("id_type")) != 3) {
                            if (cur_files.getInt(cur_files
                                    .getColumnIndex("file")) == 1) {
                                name = activity.getString(R.string.open)
                                        + " "
                                        + cur_files.getString(cur_files
                                        .getColumnIndex("name_type"));
                            } else {
                                name = activity.getString(R.string.download)
                                        + " "
                                        + cur_files.getString(cur_files
                                        .getColumnIndex("name_type"));
                            }
                        } else {
                            name = activity.getString(R.string.player_open)
                                    + " ("
                                    + cur_files.getString(cur_files
                                    .getColumnIndex("name_type")) + ")";
                        }
                        listItems.add(name);
                        cur_files.moveToNext();
                    }

                    items = listItems
                            .toArray(new CharSequence[listItems.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            activity);
                    builder.setTitle(activity.getString(R.string.select_the_action));
                    builder.setItems(items,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int item) {
                                    open_or_download(item);
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else
                Toast.makeText(activity, R.string.no_sdcard,
                        Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            main.funct.send_bug_report(activity, e, getClass().getName(),
                    183);
        }
    }

    public void open_or_download(int id) {
        try {
            if (main.funct.ExternalStorageState() == true) {
                if (cur_files.getCount() > 0) {
                    cur_files.moveToPosition(id);
                    if (cur_files.getInt(cur_files.getColumnIndex("id_type")) == 3) {
                        Intent i = new Intent(activity, player.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("id_magazine", cur_files.getInt(cur_files
                                .getColumnIndex("id_magazine")));
                        activity.startActivity(i);
                    } else {
                        start_open_or_download(cur_files.getString(cur_files
                                .getColumnIndex("name")),
                                cur_files.getInt(cur_files
                                        .getColumnIndex("file")),
                                cur_files.getString(cur_files
                                        .getColumnIndex("link")));
                    }
                }
            } else
                Toast.makeText(activity, R.string.no_sdcard,
                        Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            main.funct.send_bug_report(activity, e, getClass().getName(),
                    232);
        }
    }

    public void start_open_or_download(String name, int file_enable,
                                       String link) {
        try {
            File file = new File(main.funct.get_dir_app(activity)
                    + "/downloads/" + name);
            if (file.exists() != true) {
                if (file_enable == 1)
                    main.funct.update_file_isn(database, name, 0);
                file_enable = 0;
            } else {
                if (file_enable == 0)
                    main.funct.update_file_isn(database, name, 1);
                file_enable = 1;
            }

            if (file_enable == 1) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);

                MimeTypeMap map = MimeTypeMap.getSingleton();
                String ext = MimeTypeMap
                        .getFileExtensionFromUrl(file.getName());
                String type = map.getMimeTypeFromExtension(ext);

                if (type == null)
                    type = "*/*";
                Uri data = Uri.fromFile(file);
                intent.setDataAndType(data, type);
                activity.startActivity(intent);

            } else {
                Intent i = new Intent(activity,
                        class_downloads_files.class);
                i.putExtra("file_url", link);
                i.putExtra("file_putch", file.getAbsolutePath());
                Toast.makeText(activity,
                        activity.getString(R.string.download_task_addeded),
                        Toast.LENGTH_SHORT).show();
                activity.startService(i);
            }
        } catch (Exception e) {
            main.funct.send_bug_report(activity, e, getClass().getName(),
                    273);
        }
    }
}
