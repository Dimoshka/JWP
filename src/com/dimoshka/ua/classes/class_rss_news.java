package com.dimoshka.ua.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
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
	private String ln_prefix = "en/news";
	public SharedPreferences prefs;

	public class_rss_news(Activity activity, int id_lang, Handler handler,
			SQLiteDatabase database) {
		this.activity = activity;
		this.handler = handler;
		this.database = database;
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		get_language(id_lang);
	}

	@SuppressWarnings("deprecation")
	public Integer get_language(int id) {
		Cursor cursor = funct.get_language(database, id, activity);
		activity.startManagingCursor(cursor);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			id_ln = cursor.getInt(cursor.getColumnIndex("_id"));
			ln_prefix = cursor.getString(cursor.getColumnIndex("news_rss"));
		} else {
			id_ln = 1;
			ln_prefix = "en/news";
		}
		activity.stopManagingCursor(cursor);
		return id_ln;
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

		@SuppressLint({ "NewApi", "SimpleDateFormat" })
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {
				rssfeedprovider = new class_rss_provider();
				String feed = String.format(URL_FEED, ln_prefix);
				Log.e("JWP_" + getClass().getName(), feed);
				this.rss_list = rssfeedprovider.parse(feed, activity);

				for (int i = 0; i < rss_list.size(); i++) {
					class_rss_item rss_item = rss_list.get(i);
					String title = rss_item.getTitle();
					String link = rss_item.getLink();
					String description = rss_item.getDescription();
					String pubdate = rss_item.getPubDate();

					SimpleDateFormat format = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
					Date date = funct.get_string_to_date(pubdate,
							"EEE, dd MMM yyyy HH:mm:ss Z");

					String reg_text = "<![CDATA[(.*)]]>";
					String reg_img = "<img(.*)/>";
					String reg_img_url = "src=\"(.*\\s*.jpg)\"";
					String link_img = "";

					Pattern p = Pattern.compile(reg_text);
					Matcher mat = p.matcher(description);

					if (mat.find()) {
						description = mat.group(1);
					}

					Pattern p2 = Pattern.compile(reg_img_url);
					Matcher mat2 = p2.matcher(description);
					if (mat2.find()) {
						link_img = mat2.group(1);
					}

					Pattern p1 = Pattern.compile(reg_img);
					Matcher mat1 = p1.matcher(description);
					if (mat1.find()) {
						description = description.replace(mat1.group(0), "");

					}

					description = description.trim();

					ContentValues init = new ContentValues();
					init.put("id_lang", id_ln);
					init.put("title", title);
					init.put("link", link);
					init.put("link_img", link_img);
					init.put("description", description);
					init.put("pubdate", format.format(date));

					database.insertWithOnConflict("news", null, init,
							SQLiteDatabase.CONFLICT_IGNORE);
				}

			} catch (Exception e) {
				funct.send_bug_report(activity, e, getClass().getName(), 134);
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			this.dialog.hide();
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
