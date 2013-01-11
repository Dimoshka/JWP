package com.dimoshka.ua.classes;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;

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

	public class_rss_jwp(Activity activity, String code_lng_an, Handler handler) {
		this.activity = activity;
		this.handler = handler;
		class_sqlite dbOpenHelper = new class_sqlite(activity,
				activity.getString(R.string.db_name), Integer.valueOf(activity
						.getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();
		get_language(code_lng_an);
		get_publication();
	}

	public void get_all_feeds(ListView list) {
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

		@SuppressLint("SimpleDateFormat")
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {
				cur_type = 1;
				for (int a = 0; a < id_pub.size(); a++) {
					cur_pub = a;
					rssfeedprovider = new class_rss_provider();
					String feed = String.format(URL_FEED, code_lng,
							code_pub.get(cur_pub), code_type.get(cur_type));
					this.rss_list = rssfeedprovider.parse(feed);

					for (int i = 0; i < rss_list.size(); i++) {
						class_rss_item rss_item = rss_list.get(i);

						String name = rss_item.getguid();
						name = name.replace("." + code_type.get(cur_type), "");

						SimpleDateFormat format = new SimpleDateFormat(
								"yyyyMMdd");
						Date date = funct.get_jwp_rss_date(name,
								code_pub.get(cur_pub), code_lng);

						database.execSQL("INSERT OR IGNORE INTO magazine (name, id_pub, id_lang, img, date) VALUES ('"
								+ name
								+ "', '"
								+ id_pub.get(cur_pub)
								+ "', '"
								+ id_ln
								+ "', '0', '"
								+ format.format(date)
								+ "');");
					}

				}

			} catch (Exception e) {
				Log.e("JWP_" + getClass().getName(), e.toString());
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			this.dialog.hide();
			//onDestroy();
			handler.sendEmptyMessage(1);
		}

		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(activity, null, activity
					.getResources().getString(R.string.dialog_loaing), true);
		}
	}

	protected void onDestroy() {
		database.close();
	}

}
