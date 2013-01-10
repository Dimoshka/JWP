package com.dimoshka.ua.classes;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewDebug.IntToString;
import android.widget.ListView;
import com.dimoshka.ua.jwp.R;

public class class_jwp_rss {
	static final String URL_FEED = "http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=%s&rmn=%s&rfm=%s&rpf=&rpe=";
	// http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=U&rmn=w&rfm=m4b

	private class_functions funct;
	private class_rssfeedprovider rssfeedprovider;
	private class_rss_adapter adapter;
	private ListView list;
	private SQLiteDatabase database;
	private Activity activity;

	private Integer id_ln = 0;
	private String code_ln = "E";

	private ArrayList<Integer> id_pub = new ArrayList<Integer>();
	private ArrayList<String> code_pub = new ArrayList<String>();
	private ArrayList<Integer> id_type = new ArrayList<Integer>();
	private ArrayList<String> code_type = new ArrayList<String>();

	public class_jwp_rss(Activity activity) {
		this.activity = activity;
		class_sqlite dbOpenHelper = new class_sqlite(activity,
				activity.getString(R.string.db_name), Integer.valueOf(activity
						.getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();
		get_language("en_EN");
		get_publication();
	}

	private void get_language(String code) {
		Cursor cursor = database.rawQuery(
				"SELECT _id, code from language where code_an='" + code + "'",
				null);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			id_ln = cursor.getInt(cursor.getColumnIndex("_id"));
			code_ln = cursor.getString(cursor.getColumnIndex("code"));
		} else
			code_ln = "E";
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

	public void get_all_feeds(ListView list) {
		try {
			this.list = list;
			new ReadFeedTask().execute();
		} catch (Exception e) {
			Log.e("JWP_" + getClass().getName(), e.toString());
		}
	}

	public void refresh(List<class_rssitem> rss_list) {
		for (int i = 0; i < rss_list.size(); i++) {
			class_rssitem rss_item = rss_list.get(i);
			// database.execSQL("INSERT OR IGNORE INTO magazine (name, img) VALUES ('"
			// + rss_item.getTitle() + "', '0');");
		}

		adapter = new class_rss_adapter(activity, rss_list);
		list.setAdapter(adapter);
	}

	class ReadFeedTask extends AsyncTask<Void, Integer, List<class_rssitem>> {
		private ProgressDialog dialog;
		List<class_rssitem> rss_list = null;

		protected List<class_rssitem> doInBackground(Void... paramArrayOfVoid) {
			try {
				// funct = new class_functions();
				// if (funct.isNetworkAvailable() == true) {
				rssfeedprovider = new class_rssfeedprovider();
				String feed = String.format(URL_FEED, code_ln, code_pub.get(0),
						code_type.get(0));
				this.rss_list = rssfeedprovider.parse(feed);
				Log.e("JWP_feed", feed);
				// }
			} catch (Exception e) {
				Log.e("JWP_" + getClass().getName(), e.toString());
			}
			return rss_list;
		}

		protected void onPostExecute(List<class_rssitem> result) {
			this.dialog.hide();
			refresh(result);
		}

		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(activity, null, activity
					.getResources().getString(R.string.dialog_loaing), true);
		}
	}

}
