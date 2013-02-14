package com.dimoshka.ua.classes;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dimoshka.ua.jwp.R;

public class class_rss_news {
	static final String URL_FEED = "http://www.jw.org/%s/rss/LatestNewsList/feed.xml";

	private class_rss_provider rssfeedprovider;
	private SQLiteDatabase database;
	public class_functions funct = new class_functions();
	private Activity activity;
	private Handler handler;

	private Integer id_ln = 0;
	private String code_lng = "E";
	public SharedPreferences prefs;

	public class_rss_news(Activity activity, int id_lang, Handler handler,
			SQLiteDatabase database) {
		this.activity = activity;
		this.handler = handler;
		this.database = database;
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);

	}

	public void get_all_feeds() {
		try {
			new ReadFeedTask().execute();
		} catch (Exception e) {
			Log.e("JWP_" + getClass().getName(), e.toString());
		}
	}

	class ReadFeedTask extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog dialog;
		List<class_rss_item> rss_list = null;

		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {

				rssfeedprovider = new class_rss_provider();
				String feed = String.format(URL_FEED, code_lng, "");
				this.rss_list = rssfeedprovider.parse(feed);

				for (int i = 0; i < rss_list.size(); i++) {

					class_rss_item rss_item = rss_list.get(i);

					String name = rss_item.getguid();

				}

			} catch (Exception e) {
				Log.e("JWP_" + getClass().getName(), e.toString());
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			this.dialog.hide();
			// database.close();
			// dbOpenHelper.close();
			handler.sendEmptyMessage(1);
		}

		protected void onPreExecute() {
			this.dialog = ProgressDialog
					.show(activity,
							null,
							activity.getResources().getString(
									R.string.dialog_loaing_rss), true);
		}
	}

}
