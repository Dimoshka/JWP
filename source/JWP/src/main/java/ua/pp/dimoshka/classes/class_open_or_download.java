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
    private Cursor cursor_download = null;
    private Cursor cursor_open = null;
    private Cursor cur = null;
    private SQLiteDatabase database;
    private class_functions funct;
    private int start_id_open = 0;
    private int start_id_download = 0;
    private int start_id_player = 0;

    public class_open_or_download(Context context, SQLiteDatabase database, class_functions funct) {
        this.context = context;
        this.database = database;
        this.funct = funct;
    }

    public void dialog_show(final long id) {
        try {

            start_id_open = 0;
            start_id_download = 0;
            start_id_player = 0;

            if (funct.ExternalStorageState()) {
                cur = database.rawQuery("select favorite from magazine where _id='" + id + "'", null);
                if (cur.getCount() > 0) {
                    cur.moveToFirst();
                    List<String> listItems = new ArrayList<String>();
                    final Boolean favorite = Boolean.valueOf(cur.getInt(cur.getColumnIndex("favorite")) != 0);

                    if (favorite) {
                        listItems.add(context.getString(R.string.favorite_remove));
                    } else {
                        listItems.add(context.getString(R.string.favorite_add));
                    }

                    cursor_open = database
                            .rawQuery(
                                    "select id_type, type.name as name_type, magazine.id_pub, magazine.name as name_magazine, files.title, files.link, files.name from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
                                            + id + "' and file=1 and id_type<>6 order by files.id_type asc", null
                            );


                    if (cursor_open.getCount() > 0) {
                        cursor_open.moveToFirst();
                        start_id_open = listItems.size();
                        for (int a = 0; a < cursor_open.getCount(); a++) {
                            listItems.add(context.getString(R.string.open)
                                    + " - "
                                    + cursor_open.getString(cursor_open
                                    .getColumnIndex("name_type"))
                                    + " - "
                                    + cursor_open.getString(cursor_open
                                    .getColumnIndex("title")));
                            cursor_open.moveToNext();
                        }
                    }

                    cursor_download = database
                            .rawQuery(
                                    "select id_type, type.name as name_type, magazine.id_pub, magazine.name as name_magazine, files.title, files.link, files.name from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
                                            + id + "' and id_type<>6 group by id_type HAVING min(file)=0 order by files.id_type asc", null
                            );


                    if (cursor_download.getCount() > 0) {
                        cursor_download.moveToFirst();
                        start_id_download = listItems.size();
                        for (int i = 0; i < cursor_download.getCount(); i++) {
                            listItems.add(context.getString(R.string.download)
                                    + " "
                                    + cursor_download.getString(cursor_download
                                    .getColumnIndex("name_type")));
                            cursor_download.moveToNext();
                        }
                    }

                    cur = database.rawQuery("select id_type from files where files.id_magazine='" + id + "' and id_type=6 limit 1", null);
                    if (cur.getCount() > 0) {
                        cur.moveToFirst();
                        start_id_player = listItems.size();
                        listItems.add(context.getString(R.string.player_open));
                    }

                    CharSequence[] items = listItems
                            .toArray(new CharSequence[listItems.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setTitle(context.getString(R.string.choose_the_action));
                    builder.setItems(items,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int item) {
                                    open_or_download(item, id, favorite);
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

    void open_or_download(int item, long id, Boolean favorite) {
        try {
            if (funct.ExternalStorageState()) {
                if (item == 0) {
                    ContentValues initialValues = new ContentValues();
                    if (favorite) {
                        initialValues.put("favorite", "0");
                    } else {
                        initialValues.put("favorite", "1");
                    }
                    database.update("magazine", initialValues, "_id=?", new String[]{id + ""});
                    Intent intent = new Intent("update");
                    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
                    broadcastManager.sendBroadcast(intent);
                } else if (item == start_id_player && start_id_player > 0) {
                    Intent i = new Intent(context, player.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("id_magazine", id);
                    context.startActivity(i);
                } else if (item >= start_id_download && start_id_download > 0) {
                    cursor_download.moveToPosition(item - start_id_download);
                    select_file(id, cursor_download.getInt(cursor_download.getColumnIndex("id_type")));
                } else if (item >= start_id_open && start_id_open > 0) {
                    cursor_open.moveToPosition(item - 1);
                    start_open_or_download(cursor_open.getString(cursor_open.getColumnIndex("name")), cursor_open.getString(cursor_open.getColumnIndex("name_magazine")),
                            true,
                            cursor_open.getString(cursor_open.getColumnIndex("link")), Integer.valueOf(cursor_open.getInt(cursor_open.getColumnIndex("id_pub")))
                    );
                }
            } else
                Toast.makeText(context, R.string.no_sdcard,
                        Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    void select_file(long id, int id_type) {
        cursor_download = database
                .rawQuery(
                        "select files._id, files.name, magazine.name as name_magazine, files.link, magazine.id_pub, files.title from files left join magazine on files.id_magazine=magazine._id where files.id_magazine='"
                                + id + "' and files.id_type='" + id_type + "' and files.file=0 order by files.name asc", null
                );

        if (cursor_download.getCount() > 0) {
            cursor_download.moveToFirst();

            if (cursor_download.getCount() == 1) {
                start_open_or_download(cursor_download.getString(cursor_download.getColumnIndex("name")), cursor_download.getString(cursor_download.getColumnIndex("name_magazine")),
                        false,
                        cursor_download.getString(cursor_download.getColumnIndex("link")), Integer.valueOf(cursor_download.getInt(cursor_download.getColumnIndex("id_pub")))
                );
            } else if (cursor_download.getCount() > 1) {
                List<String> listItems = new ArrayList<String>();

                for (int i = 0; i < cursor_download.getCount(); i++) {
                    String name;
                    name = context.getString(R.string.download)
                            + " - "
                            + cursor_download.getString(cursor_download
                            .getColumnIndex("title"));
                    listItems.add(name);
                    cursor_download.moveToNext();
                }
                CharSequence[] items = listItems.toArray(new CharSequence[listItems.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.choose_the_action));
                builder.setItems(items,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                cursor_download.moveToPosition(item);
                                start_open_or_download(cursor_download.getString(cursor_download.getColumnIndex("name")), cursor_download.getString(cursor_download.getColumnIndex("name_magazine")),
                                        false,
                                        cursor_download.getString(cursor_download.getColumnIndex("link")), Integer.valueOf(cursor_download.getInt(cursor_download.getColumnIndex("id_pub")))
                                );
                            }
                        }
                );
                AlertDialog alert = builder.create();
                alert.show();
            }

        }
    }

    void start_open_or_download(String name, String name_magazine, Boolean file_enable,
                                String link, Integer id_pub) {
        try {
            String dir_path_pub;
            if (id_pub.intValue() < 4) dir_path_pub = "/journals/";
            else if (id_pub.intValue() == 4) dir_path_pub = "/books_brochures/";
            else dir_path_pub = "/video/";

            File file = new File(funct.get_dir_app() + "/downloads" + dir_path_pub + name);
            if (!file.exists()) {
                if (file_enable.booleanValue())
                    funct.update_file_isn(database, name, Integer.valueOf(0));
                file_enable = Boolean.FALSE;
            } else {
                if (!file_enable.booleanValue())
                    funct.update_file_isn(database, name, Integer.valueOf(1));
                file_enable = Boolean.TRUE;
            }

            if (file_enable.booleanValue()) {
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
