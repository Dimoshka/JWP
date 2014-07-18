package ua.pp.dimoshka.classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import ua.pp.dimoshka.jwp.R;
import ua.pp.dimoshka.jwp.player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by designers on 23.05.13.
 */
public class class_open_or_download {

    private Context context;
    private Cursor cur_files;
    private SQLiteDatabase database;
    private class_functions funct;

    public class_open_or_download(Context context, SQLiteDatabase database, class_functions funct) {
        this.context = context;
        this.database = database;
        this.funct = funct;
    }

    public void dialog_show(String _id) {
        try {
            if (funct.ExternalStorageState() == true) {
                List<String> listItems = new ArrayList<String>();
                CharSequence[] items = null;

                cur_files = database
                        .rawQuery(
                                "select id_type, file, type.name as name_type, files.name, link, files.id_magazine, magazine.id_pub from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
                                        + _id + "' group by id_type", null
                        );
                if (cur_files.getCount() > 0) {
                    cur_files.moveToFirst();
                    for (int i = 0; i < cur_files.getCount(); i++) {
                        String name = null;

                        if (cur_files.getInt(cur_files
                                .getColumnIndex("id_type")) != 3) {
                            if (cur_files.getInt(cur_files
                                    .getColumnIndex("file")) == 1) {
                                name = context.getString(R.string.open)
                                        + " "
                                        + cur_files.getString(cur_files
                                        .getColumnIndex("name_type"));
                            } else {
                                name = context.getString(R.string.download)
                                        + " "
                                        + cur_files.getString(cur_files
                                        .getColumnIndex("name_type"));
                            }
                        } else {
                            name = context.getString(R.string.player_open)
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
                            context);
                    builder.setTitle(context.getString(R.string.select_the_action));
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

    public void open_or_download(int id) {
        try {
            if (funct.ExternalStorageState()) {
                if (cur_files.getCount() > 0) {
                    cur_files.moveToPosition(id);
                    if (cur_files.getInt(cur_files.getColumnIndex("id_type")) == 3) {
                        Intent i = new Intent(context, player.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("id_magazine", cur_files.getInt(cur_files
                                .getColumnIndex("id_magazine")));
                        context.startActivity(i);
                    } else {
                        start_open_or_download(cur_files.getString(cur_files
                                        .getColumnIndex("name")),
                                cur_files.getInt(cur_files
                                        .getColumnIndex("file")) != 0,
                                cur_files.getString(cur_files
                                        .getColumnIndex("link")), cur_files.getInt(cur_files.getColumnIndex("id_pub"))
                        );
                    }
                }
            } else
                Toast.makeText(context, R.string.no_sdcard,
                        Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    public void start_open_or_download(String name, Boolean file_enable,
                                       String link, Integer id_pub) {
        try {
            String dir_path = funct.get_dir_app();
            if (id_pub != 4) dir_path += "/downloads/jornals/";
            else dir_path += "/downloads/books_brochures/";

            File dir = new File(dir_path);
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }

            File file = new File(dir.getAbsolutePath() + "/" + name);
            if (file.exists() != true) {
                if (file_enable)
                    funct.update_file_isn(database, name, 0);
                file_enable = false;
            } else {
                if (file_enable)
                    funct.update_file_isn(database, name, 1);
                file_enable = true;
            }

            if (file_enable) {
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
                context.startActivity(intent);

            } else {
                Intent i = new Intent(context,
                        service_downloads_files.class);
                i.putExtra("file_url", link);
                i.putExtra("file_putch", file.getAbsolutePath());
                Toast.makeText(context,
                        context.getString(R.string.download_task_addeded),
                        Toast.LENGTH_SHORT).show();
                context.startService(i);
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }
}
