package com.dimoshka.ua.jwp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class class_sqlite extends SQLiteOpenHelper {

	public SQLiteDatabase database;

	public class_sqlite(Context context, String databaseName, int db_version) {
		super(context, databaseName, null, db_version);
		database = this.getWritableDatabase();
	}

	public SQLiteDatabase openDataBase() {
		return database;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.i(getClass().getName(), "Start create SQLITE");
		try {
			database.execSQL("CREATE TABLE type (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, name VARCHAR(12));");
			database.execSQL("INSERT INTO [type] ([_id], [name]) VALUES (1, 'AAC');");
			database.execSQL("INSERT INTO [type] ([_id], [name]) VALUES (2, 'EPUB');");
			database.execSQL("INSERT INTO [type] ([_id], [name]) VALUES (3, 'MP3');");
			database.execSQL("INSERT INTO [type] ([_id], [name]) VALUES (4, 'PDF');");
			database.execSQL("CREATE TABLE data (_id INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, title VARCHAR(64) UNIQUE, guid VARCHAR(64) UNIQUE, pubdate DATETIME, id_t INT);");
			database.execSQL("CREATE INDEX idx_data ON data (_id ASC);");
			database.execSQL("CREATE INDEX idx_type ON type (_id ASC);");
		} catch (Exception ex) {
			Log.e(getClass().getName(), ex.toString());
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.i(getClass().getName(), "Start update SQLITE");
		// db.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
		// onCreate(db);
	}
}