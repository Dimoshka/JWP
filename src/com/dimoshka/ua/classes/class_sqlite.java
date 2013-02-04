package com.dimoshka.ua.classes;

import com.dimoshka.ua.jwp.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class class_sqlite extends SQLiteOpenHelper {

	public SQLiteDatabase database;

	public class_sqlite(Context context) {
		super(context, context.getString(R.string.db_name), null, Integer.valueOf(context.getString(R.string.db_version)));
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
			// -- Table: magazine
			database.execSQL("CREATE TABLE magazine (_id INTEGER PRIMARY KEY ASC AUTOINCREMENT UNIQUE, name VARCHAR(128) UNIQUE, id_pub INTEGER NOT NULL, id_lang INTEGER NOT NULL, img BOOLEAN DEFAULT (0), date DATE NOT NULL);");
			// -- Table: files
			database.execSQL("CREATE TABLE files (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, id_magazine INTEGER NOT NULL, id_type INTEGER NOT NULL, name VARCHAR(32) NOT NULL UNIQUE, link VARCHAR(256) NOT NULL, pubdate DATE NOT NULL, title VARCHAR(256) NOT NULL, file BOOLEAN DEFAULT (0));");
			// -- Table: language
			database.execSQL("CREATE TABLE language (_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR(64) NOT NULL UNIQUE, code VARCHAR(4) NOT NULL UNIQUE, code_an VARCHAR(6) NOT NULL UNIQUE);");
			database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an]) VALUES (1, 'English', 'E', 'en_US');");
			database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an]) VALUES (2, 'French', 'F', 'fr_FR');");
			database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an]) VALUES (3, 'Русский', 'U', 'ru_RU');");
			database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an]) VALUES (4, 'Українська', 'K', 'uk_UA');");
			database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an]) VALUES (5, 'Deutsch', 'X', 'de_DE');");
			database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an]) VALUES (6, 'Spanish', 'S', 'es_ES');");
			database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an]) VALUES (7, '漢語繁體字', 'CH', 'zh_CN');");
			database.execSQL("INSERT INTO [language] ([_id], [name], [code], [code_an]) VALUES (8, '汉语简化字', 'CHS', 'zh_TW');");
			// -- Index: idx_files
			database.execSQL("CREATE INDEX idx_files ON files (id_magazine COLLATE NOCASE ASC, id_type COLLATE NOCASE ASC);");
		} catch (Exception ex) {
			Log.e("JWP" + getClass().getName(), ex.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.i("JWP" + getClass().getName(), "Start update SQLITE");
		database.execSQL("DROP TABLE IF EXISTS type");
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