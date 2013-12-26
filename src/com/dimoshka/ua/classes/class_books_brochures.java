package com.dimoshka.ua.classes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.dimoshka.ua.jwp.R;

import java.io.File;
import java.util.List;

public class class_books_brochures {
    private SQLiteDatabase database;
    public class_functions funct = new class_functions();
    private Activity activity;
    private Cursor cursor;
    private Handler handler;
    private AsyncTask task;

    public class_books_brochures(Activity activity, Handler handler,
                                 SQLiteDatabase database) {
        this.activity = activity;
        this.handler = handler;
        this.database = database;
    }

    public void verify_all_img() {
        task = new verify_img().execute();
    }

    class verify_img extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog;
        List<class_rss_item> rss_list = null;

        @Override
        protected Void doInBackground(Void... paramArrayOfVoid) {
            try {
                if (funct.ExternalStorageState()) {
                    cursor = database
                            .rawQuery(
                                    "select _id from magazine where id_pub='4';",
                                    null);
                    if (cursor.getCount() == 0) {
                        add_books_and_brochures();
                    }
                    cursor = database
                            .rawQuery(
                                    "select _id, name, img, link_img from magazine where img=0 and id_pub='4';",
                                    null);
                    cursor.moveToFirst();
                    String dir = funct.get_dir_app(activity) + "/img/";

                    File Directory = new File(dir);
                    if (!Directory.isDirectory()) {
                        Directory.mkdirs();
                    }

                    for (int i = 0; i < cursor.getCount(); i++) {
                        if (isCancelled()) break;
                        String name = cursor.getString(cursor
                                .getColumnIndex("name"));
                        String link_img = cursor.getString(cursor
                                .getColumnIndex("link_img"));
                        ContentValues initialValues = new ContentValues();

                        File imgFile = new File(dir + "/img/" + name + ".jpg");
                        if (!imgFile.exists()) {
                            Log.i("JWP_image", name + " - not found!");

                            try {
                                if (funct.load_img(activity, dir, name, link_img)) {
                                    Log.i("JWP_image", name
                                            + " - file download complete!");
                                    initialValues.put("img", "1");
                                    String[] args = {String
                                            .valueOf(cursor.getString(cursor
                                                    .getColumnIndex("_id")))};
                                    database.update("magazine", initialValues,
                                            "_id=?", args);
                                }

                            } catch (Exception e) {
                                Log.e("JWP_" + getClass().getName(),
                                        e.toString());
                            }
                        } else {
                            Log.i("JWP_image", name + " found!");
                            initialValues.put("img", "1");
                            String[] args = {String.valueOf(cursor
                                    .getString(cursor.getColumnIndex("_id")))};
                            database.update("magazine", initialValues, "_id=?",
                                    args);
                        }
                        cursor.moveToNext();
                    }
                }
            } catch (Exception e) {
                funct.send_bug_report(activity, e, getClass().getName(), 154);
            }
            return null;
        }

        public void add_books_and_brochures() {
            try {
                Log.e("JWP_sql", "start add Books and brochures");
                //database.beginTransaction();
                Log.e("JWP_sql", "start add in magazine");
                // -- INSERT: Books and brochures
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES (1, 'pc_U', 'Прочный мир и счастье. Как их найти', 4, '3', 0, 'http://www.jw.org/assets/a/pc/pc_U/pc_U_md.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES (2, 'ca14_U', 'Календарь Свидетелей Иеговы 2014', 4, '3', 0, 'http://assets.jw.org/assets/a/ca14/ca14_U/ca14_U_md.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES (3, 'es14_U', 'Исследовать Писания каждый день 2014', 4, '3', 0, 'http://assets.jw.org/assets/a/es14/es14_U/es14_U_md.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES (4, 'T-35_U', 'Будут ли умершие жить снова?', 4, '3', 0, 'http://assets.jw.org/assets/a/t-35/t-35_U/T-35_U_md.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES ('5', 'yi_U', 'Молодежь, чему вы посвятите свою жизнь?', 4, '3', 0, 'http://assets.jw.org/assets/a/yi/yi_U/yi_U_md.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES ('6', 'yp2_U', 'Ответы на твои вопросы. Том 2', 4, '3', 0, 'http://www.jw.org/assets/a/yp2/yp2_U/yp2_U_xs.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES ('7', 'fg_U', 'Добрая весть от Бога', 4, '3', 0, 'http://assets.jw.org/assets/a/fg/fg_U/fg_U_md.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES ('8', 'jl_U', 'Кто сегодня исполняет волю Иеговы?', 4, '3', 0, 'http://assets.jw.org/assets/a/jl/jl_U/jl_U_md.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES ('9', 'be_U', 'Учимся в Школе теократического служения', 4, '3', 0, 'http://assets.jw.org/assets/a/be/be_U/be_U_md.jpg', '20131225');");
                database.execSQL("INSERT INTO [magazine] ([_id], [name], [title], [id_pub], [id_lang], [img], [link_img], [date]) VALUES ('10', 'yp1_U', 'Ответы на твои вопросы. Том 1', 4, '3', 0, 'http://www.jw.org/assets/a/yp1/yp1_U/yp1_U_xs.jpg', '20131225');");

                Log.e("JWP_sql", "end add Books and brochures");
                Log.e("JWP_sql", "start add in files");

                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('1', '2', 'pc_U.pdf', 'http://download.jw.org/files/media_books/49/pc_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('2', '2', 'ca14_U.pdf', 'http://download.jw.org/files/media_books/a5/ca14_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('3', '1', 'es14_U.epub', 'http://download.jw.org/files/media_books/1c/es14_U.epub', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('3', '2', 'es14_U', 'http://download.jw.org/files/media_books/ed/es14_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('4', '1', 'T-35_U.epub', 'http://download.jw.org/files/media_books/e1/T-35_U.epub', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('4', '2', 'T-35_U.pdf', 'http://download.jw.org/files/media_books/13/T-35_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('4', '3', 'T-35_U_01.mp3', 'http://download.jw.org/files/media_books/3b/T-35_U_01.mp3', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('4', '4', 'T-35_U.m4b', 'http://download.jw.org/files/media_books/47/T-35_U.m4b', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('5', '2', 'yi_U.pdf', 'http://download.jw.org/files/media_books/68/yi_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('6', '2', 'yp2_U.pdf', 'http://download.jw.org/files/media_books/a7/yp2_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '2', 'fg_U.pdf', 'http://download.jw.org/files/media_books/27/fg_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '1', 'fg_U.epub', 'http://download.jw.org/files/media_books/0e/fg_U.epub', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '4', 'fg_U.m4b', 'http://download.jw.org/files/media_books/83/fg_U.m4b', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_01.mp3', 'http://download.jw.org/files/media_books/17/fg_U_01.mp3', '20131225', 'ПРЕДИСЛОВИЕ — Как пользоваться этой брошюрой', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_02.mp3', 'http://download.jw.org/files/media_books/2d/fg_U_02.mp3', '20131225', 'УРОК 01 — Добрая весть о чем?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_03.mp3', 'http://download.jw.org/files/media_books/58/fg_U_03.mp3', '20131225', 'УРОК 02 — Кто такой Бог?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_04.mp3', 'http://download.jw.org/files/media_books/d0/fg_U_04.mp3', '20131225', 'УРОК 03 — Откуда мы знаем, что Библия — от Бога?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_05.mp3', 'http://download.jw.org/files/media_books/dd/fg_U_05.mp3', '20131225', 'УРОК 04 — Кто такой Иисус Христос?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_06.mp3', 'http://download.jw.org/files/media_books/01/fg_U_06.mp3', '20131225', 'УРОК 05 — Что будет с землей?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_07.mp3', 'http://download.jw.org/files/media_books/8e/fg_U_07.mp3', '20131225', 'УРОК 06 — Какая есть надежда для тех, кто умер?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_08.mp3', 'http://download.jw.org/files/media_books/01/fg_U_08.mp3', '20131225', 'УРОК 07 — Что такое Царство Бога?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_09.mp3', 'http://download.jw.org/files/media_books/ba/fg_U_09.mp3', '20131225', 'УРОК 08 — Почему Бог допускает зло и страдания?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_10.mp3', 'http://download.jw.org/files/media_books/81/fg_U_10.mp3', '20131225', 'УРОК 09 — В чем секрет семейного счастья?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_11.mp3', 'http://download.jw.org/files/media_books/09/fg_U_11.mp3', '20131225', 'УРОК 10 — Как найти истинную религию?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_12.mp3', 'http://download.jw.org/files/media_books/22/fg_U_12.mp3', '20131225', 'УРОК 11 — Почему стоит жить по библейским принципам?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_13.mp3', 'http://download.jw.org/files/media_books/d1/fg_U_13.mp3', '20131225', 'УРОК 12 — Как стать ближе к Богу?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_14.mp3', 'http://download.jw.org/files/media_books/3c/fg_U_14.mp3', '20131225', 'УРОК 13 — Что ждет религию?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_15.mp3', 'http://download.jw.org/files/media_books/a7/fg_U_15.mp3', '20131225', 'УРОК 14 — Для чего нужна Божья организация?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('7', '3', 'fg_U_16.mp3', 'http://download.jw.org/files/media_books/89/fg_U_16.mp3', '20131225', 'УРОК 15 — Почему не стоит останавливаться на достигнутом?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '1', 'jl_U.epub', 'http://download.jw.org/files/media_books/63/jl_U.epub', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '2', 'jl_U.pdf', 'http://download.jw.org/files/media_books/bc/jl_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '4', 'jl_U.m4b', 'http://download.jw.org/files/media_books/68/jl_U.m4b', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_01.mp3', 'http://download.jw.org/files/media_books/db/jl_U_01.mp3', '20131225', 'СОДЕРЖАНИЕ — Свидетели Иеговы', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_02.mp3', 'http://download.jw.org/files/media_books/fb/jl_U_02.mp3', '20131225', 'ПРЕДИСЛОВИЕ — В чем состоит воля Бога?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_03.mp3', 'http://download.jw.org/files/media_books/43/jl_U_03.mp3', '20131225', 'УРОК 01 — Кто такие Свидетели Иеговы?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_04.mp3', 'http://download.jw.org/files/media_books/46/jl_U_04.mp3', '20131225', 'УРОК 02 — Почему мы называемся Свидетелями Иеговы?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_05.mp3', 'http://download.jw.org/files/media_books/f3/jl_U_05.mp3', '20131225', 'УРОК 03 — Как библейская истина была открыта заново?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_06.mp3', 'http://download.jw.org/files/media_books/fb/jl_U_06.mp3', '20131225', 'УРОК 04 — Почему мы выпустили «Перевод нового мира»?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_07.mp3', 'http://download.jw.org/files/media_books/7e/jl_U_07.mp3', '20131225', 'УРОК 05 — Что вас ждет на наших христианских встречах?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_08.mp3', 'http://download.jw.org/files/media_books/c1/jl_U_08.mp3', '20131225', 'УРОК 06 — Как нас обогащает общение с соверующими?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_09.mp3', 'http://download.jw.org/files/media_books/de/jl_U_09.mp3', '20131225', 'УРОК 07 — Как проходят наши встречи?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_10.mp3', 'http://download.jw.org/files/media_books/ec/jl_U_10.mp3', '20131225', 'УРОК 08 — Почему мы нарядно одеваемся на наши встречи?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_11.mp3', 'http://download.jw.org/files/media_books/31/jl_U_11.mp3', '20131225', 'УРОК 09 — Как лучше всего готовиться к встречам?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_12.mp3', 'http://download.jw.org/files/media_books/f8/jl_U_12.mp3', '20131225', 'УРОК 10 — Что такое семейное поклонение?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_13.mp3', 'http://download.jw.org/files/media_books/cf/jl_U_13.mp3', '20131225', 'УРОК 11 — Почему мы проводим конгрессы?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_14.mp3', 'http://download.jw.org/files/media_books/4f/jl_U_14.mp3', '20131225', 'УРОК 12 — Как организована наша проповедническая деятельность?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_15.mp3', 'http://download.jw.org/files/media_books/67/jl_U_15.mp3', '20131225', 'УРОК 13 — Кто такой пионер?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_16.mp3', 'http://download.jw.org/files/media_books/94/jl_U_16.mp3', '20131225', 'УРОК 14 — Что такое миссионерское служение?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_17.mp3', 'http://download.jw.org/files/media_books/71/jl_U_17.mp3', '20131225', 'УРОК 15 — Как старейшины служат собранию?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_18.mp3', 'http://download.jw.org/files/media_books/3f/jl_U_18.mp3', '20131225', 'УРОК 16 — Для чего нужны служебные помощники?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_19.mp3', 'http://download.jw.org/files/media_books/55/jl_U_19.mp3', '20131225', 'УРОК 17 — Как нам помогают разъездные надзиратели?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_20.mp3', 'http://download.jw.org/files/media_books/0a/jl_U_20.mp3', '20131225', 'УРОК 18 — Как мы помогаем нашим братьям во время стихийных бедствий?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_21.mp3', 'http://download.jw.org/files/media_books/07/jl_U_21.mp3', '20131225', 'УРОК 19 — Кто такой «верный и благоразумный раб»?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_22.mp3', 'http://download.jw.org/files/media_books/53/jl_U_22.mp3', '20131225', 'УРОК 20 — Как Руководящий совет действует в наши дни?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_23.mp3', 'http://download.jw.org/files/media_books/c0/jl_U_23.mp3', '20131225', 'УРОК 21 — Что такое Вефиль?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_24.mp3', 'http://download.jw.org/files/media_books/6e/jl_U_24.mp3', '20131225', 'УРОК 22 — Какая работа выполняется в филиале?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_25.mp3', 'http://download.jw.org/files/media_books/ef/jl_U_25.mp3', '20131225', 'УРОК 23 — Как пишется и переводится наша литература?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_26.mp3', 'http://download.jw.org/files/media_books/95/jl_U_26.mp3', '20131225', 'УРОК 24 — Как финансируется наша всемирная деятельность?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_27.mp3', 'http://download.jw.org/files/media_books/47/jl_U_27.mp3', '20131225', 'УРОК 25 — Залы Царства — для чего и как они строятся?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_28.mp3', 'http://download.jw.org/files/media_books/44/jl_U_28.mp3', '20131225', 'УРОК 26 — Как мы заботимся о Зале Царства?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_29.mp3', 'http://download.jw.org/files/media_books/ba/jl_U_29.mp3', '20131225', 'УРОК 27 — Для чего нужна библиотека Зала Царства?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_30.mp3', 'http://download.jw.org/files/media_books/e2/jl_U_30.mp3', '20131225', 'УРОК 28 — Что можно найти на нашем сайте?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('8', '3', 'jl_U_31.mp3', 'http://download.jw.org/files/media_books/9f/jl_U_31.mp3', '20131225', 'Будете ли вы исполнять волю Иеговы?', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('9', '2', 'be_U.pdf', 'http://download.jw.org/files/media_books/fa/be_U.pdf', '20131225', '', 0);");
                database.execSQL("INSERT INTO [files] ([id_magazine], [id_type], [name], [link], [pubdate], [title], [file]) VALUES ('10', '2', 'yp1_U.pdf', 'http://download.jw.org/files/media_books/2b/yp1_U.pdf', '20131225', '', 0);");
                Log.e("JWP_sql", "end add in files");
                //database.endTransaction();
                Log.e("JWP_sql", "end add Books and brochures");

            } catch (Exception e) {
                funct.send_bug_report(activity, e, getClass().getName(), 196);
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog != null)
                dialog.dismiss();
            cursor.close();
            handler.sendEmptyMessage(1);
        }

        @Override
        protected void onPreExecute() {
            this.dialog = ProgressDialog
                    .show(activity,
                            activity.getResources().getString(
                                    R.string.books_brochures),
                            activity.getResources().getString(
                                    R.string.dialog_loaing_img), true, true, new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface pd) {
                            task.cancel(true);
                        }
                    });
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
