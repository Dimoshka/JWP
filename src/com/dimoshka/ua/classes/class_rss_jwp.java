package com.dimoshka.ua.classes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.dimoshka.ua.jwp.R;

public class class_rss_jwp {
	static final String URL_FEED = "http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=%s&rmn=%s&rfm=%s&rpf=&rpe=";
	// http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=U&rmn=w&rfm=m4b
	// wp_U_20130301

	private class_rss_provider rssfeedprovider;
	private SQLiteDatabase database;
	public class_functions funct = new class_functions();
	private Activity activity;
	private Handler handler;

	private Integer id_ln = 0;
	private String code_lng = "E";

	private ArrayList<Integer> id_pub = new ArrayList<Integer>();
	private ArrayList<String> code_pub = new ArrayList<String>();
	private Integer cur_pub = 0;
	private ArrayList<Integer> id_type = new ArrayList<Integer>();
	private ArrayList<String> code_type = new ArrayList<String>();
	private Integer cur_type = 0;
	private class_sqlite dbOpenHelper;

	public class_rss_jwp(Activity activity, String code_lng_an, Handler handler) {
		this.activity = activity;
		this.handler = handler;
		dbOpenHelper = new class_sqlite(activity,
				activity.getString(R.string.db_name), Integer.valueOf(activity
						.getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();
		get_language(code_lng_an);
		get_publication();
		database.close();
	}

	public void get_all_feeds() {
		try {
			new ReadFeedTask().execute();
		} catch (Exception e) {
			Log.e("JWP_" + getClass().getName(), e.toString());
		}
	}

	private void get_language(String code) {
		Cursor cursor = database.rawQuery(
				"SELECT _id, code from language where code_an='" + code + "'",
				null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			id_ln = cursor.getInt(cursor.getColumnIndex("_id"));
			code_lng = cursor.getString(cursor.getColumnIndex("code"));
		} else {
			id_ln = 1;
			code_lng = "E";
		}
		cursor.close();
	}

	private void get_publication() {
		Cursor cursor_type = database.query("type", new String[] { "_id",
				"code" }, null, null, null, null, "_id");
		Cursor cursor_pub = database.query("publication", new String[] { "_id",
				"code" }, null, null, null, null, "_id");
		cursor_type.moveToFirst();
		cursor_pub.moveToFirst();

		for (int i = 0; i < cursor_type.getCount(); i++) {
			id_type.add(cursor_type.getInt(cursor_type.getColumnIndex("_id")));
			code_type.add(cursor_type.getString(cursor_type
					.getColumnIndex("code")));
			cursor_type.moveToNext();
		}

		for (int i = 0; i < cursor_pub.getCount(); i++) {
			id_pub.add(cursor_pub.getInt(cursor_pub.getColumnIndex("_id")));
			code_pub.add(cursor_pub.getString(cursor_pub.getColumnIndex("code")));
			cursor_pub.moveToNext();
		}

		cursor_type.close();
		cursor_pub.close();
	}

	class ReadFeedTask extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog dialog;
		List<class_rss_item> rss_list = null;

		@SuppressLint({ "SimpleDateFormat", "NewApi" })
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {

				for (int a = 0; a < id_pub.size(); a++) {
					for (int b = 0; b < id_type.size(); b++) {
						cur_type = b;
						cur_pub = a;
						rssfeedprovider = new class_rss_provider();
						String feed = String.format(URL_FEED, code_lng,
								code_pub.get(cur_pub), code_type.get(cur_type));
						this.rss_list = rssfeedprovider.parse(feed);

						for (int i = 0; i < rss_list.size(); i++) {

							class_rss_item rss_item = rss_list.get(i);

							String name = rss_item.getguid();
							name = name.replace("." + code_type.get(cur_type),
									"");

							String date_str = name.replace(
									code_pub.get(cur_pub), "");
							date_str = date_str.replace(code_lng, "");
							date_str = date_str.replace("_", "");

							if (date_str.length() > 8
									|| (cur_pub == 2 && date_str.length() > 6))
								name = name.substring(0, name.length() - 3);

							SimpleDateFormat format = new SimpleDateFormat(
									"yyyyMMdd");
							Date date = funct.get_jwp_rss_date(name,
									code_pub.get(cur_pub), code_lng);

							Cursor cur = database.rawQuery(
									"select _id from magazine where `name` = '"
											+ name + "'", null);
							long id_magazine = 0;
							if (cur.getCount() > 0) {
								cur.moveToFirst();
								id_magazine = cur.getLong(cur
										.getColumnIndex("_id"));
								Log.e("JWP_INSERT MAG + ", rss_item.getguid()
										+ " - " + id_magazine);
							} else {
								ContentValues init1 = new ContentValues();
								init1.put("name", name);
								init1.put("id_pub", id_pub.get(cur_pub));
								init1.put("id_lang", id_ln);
								init1.put("img", 0);
								init1.put("date", format.format(date));

								id_magazine = database.insert("magazine", null,
										init1);

								Log.e("JWP_INSERT MAG - ", rss_item.getguid()
										+ " - " + id_magazine);
							}

							cur.close();
							ContentValues init2 = new ContentValues();
							init2.put("id_magazine", id_magazine);
							init2.put("id_type", id_type.get(cur_type));
							init2.put("name", rss_item.getguid());
							init2.put("link", rss_item.getLink());
							init2.put("pubdate", rss_item.getPubDate());
							init2.put("title", rss_item.getTitle());
							init2.put("file", 0);

							database.insertWithOnConflict("files", null, init2,
									SQLiteDatabase.CONFLICT_IGNORE);

						}

					}
				}

			} catch (Exception e) {
				Log.e("JWP_" + getClass().getName(), e.toString());
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			this.dialog.hide();
			database.close();
			handler.sendEmptyMessage(1);
		}

		protected void onPreExecute() {
			database = dbOpenHelper.openDataBase();
			this.dialog = ProgressDialog.show(activity, null, activity
					.getResources().getString(R.string.dialog_loaing), true);
		}
	}

}
