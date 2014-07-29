package ua.pp.dimoshka.classes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import ua.pp.dimoshka.jwp.R;

public class class_sqlite extends SQLiteAssetHelper {

    private SQLiteDatabase database;

    public class_sqlite(Context context) {
        super(context, context.getString(R.string.db_name), null, Integer
                .valueOf(context.getString(R.string.db_version)).intValue());
        database = this.getWritableDatabase();
    }

    public SQLiteDatabase openDataBase() {
        return database;
    }
}