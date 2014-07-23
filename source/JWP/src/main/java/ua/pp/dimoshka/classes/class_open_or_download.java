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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ua.pp.dimoshka.jwp.R;
import ua.pp.dimoshka.jwp.player;

public class class_open_or_download {

    private Context context;
    private Cursor cursor;
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
                                "select files._id, id_type, file, type.name as name_type, files.name, link, files.id_magazine, magazine.id_pub, magazine.name as name_magazine from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
                                        + id + "' group by id_type order by files.id_type", null
                        );
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        String name;

                        if (cursor.getInt(cursor
                                .getColumnIndex("id_type")) != 3) {
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
                if (cursor.getCount() > 0) {
                    cursor.moveToPosition(id);
                    if (cursor.getInt(cursor.getColumnIndex("id_type")) == 3) {
                        Intent i = new Intent(context, player.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("id_magazine", cursor.getInt(cursor
                                .getColumnIndex("id_magazine")));
                        context.startActivity(i);
                    } else {
                        start_open_or_download(cursor.getString(cursor
                                        .getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("name_magazine")),
                                cursor.getInt(cursor
                                        .getColumnIndex("file")) != 0,
                                cursor.getString(cursor
                                        .getColumnIndex("link")), cursor.getInt(cursor.getColumnIndex("id_pub"))
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

    public void start_open_or_download(String name, String name_magazine, Boolean file_enable,
                                       String link, Integer id_pub) {
        try {
            String dir_path_pub;
            if (id_pub != 4) dir_path_pub = "/jornals/";
            else dir_path_pub = "/books_brochures/";

            File file = new File(funct.get_dir_app() + "/downloads" + dir_path_pub + name);
            if (!file.exists()) {
                if (file_enable)
                    funct.update_file_isn(database, name, 0);
                file_enable = false;
            } else {
                if (!file_enable)
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
                        context.getString(R.string.download_task_addeded),
                        Toast.LENGTH_SHORT).show();
                context.startService(i);
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }
}
