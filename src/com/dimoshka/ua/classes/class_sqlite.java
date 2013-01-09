package com.dimoshka.ua.classes;

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
			// -- Table: type
			database.execSQL("CREATE TABLE type (_id  INTEGER        PRIMARY KEY AUTOINCREMENT UNIQUE, name VARCHAR( 16 )  UNIQUE);");
			database.execSQL("INSERT INTO [type] ([_id], [name]) VALUES (1, 'EPUB');");
			database.execSQL("INSERT INTO [type] ([_id], [name]) VALUES (2, 'PDF');");
			database.execSQL("INSERT INTO [type] ([_id], [name]) VALUES (3, 'MP3');");
			database.execSQL("INSERT INTO [type] ([_id], [name]) VALUES (4, 'AAC');");
			// -- Table: files
			database.execSQL("CREATE TABLE files ( _id         INTEGER         PRIMARY KEY AUTOINCREMENT  UNIQUE,  id_magazine INTEGER         NOT NULL,   id_type     INTEGER         NOT NULL,    name        VARCHAR( 32 )   NOT NULL    UNIQUE,  link        VARCHAR( 256 )  NOT NULL,   pubdate     DATETIME        NOT NULL, title       VARCHAR( 256 )  NOT NULL, id_language INTEGER         NOT NULL,   file        BOOLEAN         DEFAULT ( 0 ) );");
			// -- Table: magazine
			database.execSQL("CREATE TABLE magazine (  _id  INTEGER         PRIMARY KEY ASC AUTOINCREMENT UNIQUE,name VARCHAR( 128 )  UNIQUE, img  BOOLEAN         DEFAULT ( 0 ));");
			database.execSQL("INSERT INTO [magazine] ([_id], [name], [img]) VALUES (1, 'wp_2012_01', 0);");
			database.execSQL("INSERT INTO [magazine] ([_id], [name], [img]) VALUES (4, 'wp_2012_02', 0);");
			database.execSQL("INSERT INTO [magazine] ([_id], [name], [img]) VALUES (5, 'wp_2012_03', 0);");
			database.execSQL("INSERT INTO [magazine] ([_id], [name], [img]) VALUES (6, 'wp_2012_04', 0);");
			database.execSQL("INSERT INTO [magazine] ([_id], [name], [img]) VALUES (7, 'wp_2012_05', 0);");
			// -- Table: language
			database.execSQL("CREATE TABLE language (  _id  INTEGER        PRIMARY KEY AUTOINCREMENT, name VARCHAR( 64 )  NOT NULL   UNIQUE, code VARCHAR( 3 )   NOT NULL UNIQUE );");
			// -- Index: idx_files
			database.execSQL("CREATE INDEX idx_files ON files (  id_magazine COLLATE NOCASE ASC, id_type     COLLATE NOCASE ASC, id_language COLLATE NOCASE ASC );");
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