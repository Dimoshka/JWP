package ua.pp.dimoshka.classes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ua.pp.dimoshka.jwp.R;

public class class_sqlite extends SQLiteOpenHelper {
    public SQLiteDatabase database;
    private class_functions funct;

    public class_sqlite(Context context, class_functions funct) {
        super(context, context.getString(R.string.db_name), null, Integer
                .valueOf(context.getString(R.string.db_version)));
        this.funct = funct;
        database = this.getWritableDatabase();
    }


    public SQLiteDatabase openDataBase() {
        return database;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(getClass().getName(), "Start create SQLITE");
        try {
            // -- Table: type
            database.execSQL("CREATE TABLE type (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, name VARCHAR(16) UNIQUE, code VARCHAR(6) NOT NULL UNIQUE);");
            database.execSQL("INSERT INTO [type] ([_id], [name], [code]) VALUES (1, 'EPUB', 'epub');");
            database.execSQL("INSERT INTO [type] ([_id], [name], [code]) VALUES (2, 'PDF', 'pdf');");
            database.execSQL("INSERT INTO [type] ([_id], [name], [code]) VALUES (3, 'MP3', 'mp3');");
            database.execSQL("INSERT INTO [type] ([_id], [name], [code]) VALUES (4, 'AAC', 'm4b');");
            // -- Table: publication
            database.execSQL("CREATE TABLE publication (_id  INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, name VARCHAR(128) NOT NULL UNIQUE, code VARCHAR(3) NOT NULL UNIQUE);");
            database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (1, 'THE WATCHTOWER (STUDY EDITION)', 'w');");
            database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (2, 'THE WATCHTOWER', 'wp');");
            database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (3, 'AWAKE!', 'g');");
            database.execSQL("INSERT INTO [publication] ([_id], [name], [code]) VALUES (4, 'Books and brochures', 'b');");
            // -- Table: magazine
            database.execSQL("CREATE TABLE magazine (_id INTEGER PRIMARY KEY ASC AUTOINCREMENT UNIQUE, name VARCHAR(128) UNIQUE, title VARCHAR(128), id_pub INTEGER NOT NULL, id_lang INTEGER NOT NULL, img BOOLEAN DEFAULT (0), link_img VARCHAR(256), date DATE NOT NULL);");
            // -- Table: files
            database.execSQL("CREATE TABLE files (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, id_magazine INTEGER NOT NULL, id_type INTEGER NOT NULL, name VARCHAR(32) NOT NULL UNIQUE, link VARCHAR(256) NOT NULL, pubdate DATE NOT NULL, title VARCHAR(256) NOT NULL, file BOOLEAN DEFAULT (0));");
            // -- Table: news
            database.execSQL("CREATE TABLE news (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, id_lang INTEGER NOT NULL, title VARCHAR(256) NOT NULL UNIQUE, link VARCHAR(256) NOT NULL UNIQUE, link_img VARCHAR(256), description VARCHAR(256) NOT NULL, pubdate DATETIME NOT NULL, img BOOLEAN DEFAULT (0));");
            // -- Table: language
            database.execSQL("CREATE TABLE language (_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(64) NOT NULL UNIQUE, code VARCHAR(4) NOT NULL UNIQUE, code_an VARCHAR(6) NOT NULL UNIQUE, news_rss VARCHAR(24) NOT NULL);");
            database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (1, 'English', 'E', 'en', 'en/news');");
            database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (2, 'French', 'F', 'fr', 'fr/actualites');");
            database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (3, 'Русский', 'U', 'ru', 'ru/%D0%BD%D0%BE%D0%B2%D0%BE%D1%81%D1%82%D0%B8');");
            database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (4, 'Українська', 'K', 'uk', 'uk/%D0%BD%D0%BE%D0%B2%D0%B8%D0%BD%D0%B8');");
            database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (5, 'Deutsch', 'X', 'de', 'de/aktuelle-meldungen');");
            database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (6, 'Spanish', 'S', 'es', 'es/noticias');");
            database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (7, '漢語繁體字', 'CH', 'zh', 'zh-hant/%E6%96%B0%E8%81%9E');");
            database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an], [news_rss]) VALUES (8, '汉语简化字', 'CHS', 'zhc', 'zh-hans/%E6%96%B0%E9%97%BB');");
            // -- Index
            database.execSQL("CREATE INDEX idx_magazine ON magazine (id_pub COLLATE NOCASE ASC, id_lang COLLATE NOCASE ASC);");
            database.execSQL("CREATE INDEX idx_files ON files (id_magazine COLLATE NOCASE ASC, id_type COLLATE NOCASE ASC);");
        } catch (Exception ex) {
            Log.e("JWP", ex.toString());
            funct.send_bug_report(ex);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Log.i("JWP" + getClass().getName(), "Start update SQLITE");
        database.execSQL("DROP TABLE IF EXISTS type");
        database.execSQL("DROP TABLE IF EXISTS news");
        database.execSQL("DROP TABLE IF EXISTS files");
        database.execSQL("DROP TABLE IF EXISTS language");
        database.execSQL("DROP TABLE IF EXISTS publication");
        database.execSQL("DROP TABLE IF EXISTS magazine");
        onCreate(database);
    }

    public void close() {
        if (database != null) {
            database.close();
        }
    }
}