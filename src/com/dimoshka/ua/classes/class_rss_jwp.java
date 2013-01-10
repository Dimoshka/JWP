package com.dimoshka.ua.classes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;
import com.dimoshka.ua.jwp.R;

public class class_rss_jwp {
	static final String URL_FEED = "http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=%s&rmn=%s&rfm=%s&rpf=&rpe=";
	static final String URL_IMG = "http://www.jw.org/assets/a/{code_pub_shot}{YY}/{YYYYMMDD}/{code_pub_shot}{YY}_{YYYYMMDD}_{code_lng}/{code_pub}_{code_lng}_{YYYYMMDD}.prd_md.jpg";

	// http://www.jw.org/assets/a/g13/201303/g13_201303_U/g_U_201303prd_md.jpg
	// http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=U&rmn=w&rfm=m4b
	// wp_U_20130301

	private class_rss_provider rssfeedprovider;
	private class_rss_adapter adapter;
	private ListView list;
	private SQLiteDatabase database;
	public class_functions funct = new class_functions();
	private Activity activity;

	private Integer id_ln = 0;
	private String code_lng = "E";

	private ArrayList<Integer> id_pub = new ArrayList<Integer>();
	private ArrayList<String> code_pub = new ArrayList<String>();
	private Integer cur_pub = 0;
	private ArrayList<Integer> id_type = new ArrayList<Integer>();
	private ArrayList<String> code_type = new ArrayList<String>();
	private Integer cur_type = 0;

	public class_rss_jwp(Activity activity) {
		this.activity = activity;
		class_sqlite dbOpenHelper = new class_sqlite(activity,
				activity.getString(R.string.db_name), Integer.valueOf(activity
						.getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();
		get_language("ru_RU");
		get_publication();
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

	public void get_all_feeds(ListView list) {
		try {
			this.list = list;
			new ReadFeedTask().execute();
		} catch (Exception e) {
			Log.e("JWP_" + getClass().getName(), e.toString());
		}
	}

	public void refresh() {
		adapter = new class_rss_adapter(activity);
		list.setAdapter(adapter);
	}

	class ReadFeedTask extends AsyncTask<Void, Integer, List<class_rss_item>> {
		private ProgressDialog dialog;
		List<class_rss_item> rss_list = null;

		protected List<class_rss_item> doInBackground(Void... paramArrayOfVoid) {
			try {
				cur_type = 1;
				cur_pub = 1;

				rssfeedprovider = new class_rss_provider();
				String feed = String.format(URL_FEED, code_lng,
						code_pub.get(cur_pub), code_type.get(cur_type));
				this.rss_list = rssfeedprovider.parse(feed);

				for (int i = 0; i < rss_list.size(); i++) {
					class_rss_item rss_item = rss_list.get(i);

					String name = rss_item.getguid();
					name = name.replace("." + code_type.get(cur_type), "");

					Boolean img_b = get_image(name);
					int img_i = img_b? 1 : 0;

					Log.i("Insert",
							name + "-" + id_ln + "-" + id_pub.get(cur_pub));
					database.execSQL("INSERT OR IGNORE INTO magazine (name, id_pub, id_lang, img) VALUES ('"
							+ name
							+ "', '"
							+ id_pub.get(cur_pub)
							+ "', '"
							+ id_ln + "', '" + img_i + "');");
				}

			} catch (Exception e) {
				Log.e("JWP_" + getClass().getName(), e.toString());
			}
			return rss_list;
		}

		@SuppressLint("SimpleDateFormat")
		private Boolean get_image(String name) {
			try {
				if (funct.ExternalStorageState()) {
					String dir = Environment.getExternalStorageDirectory()
							+ "/"
							+ activity.getResources().getString(
									R.string.app_dir) + "/img/";

					File Directory = new File(dir);
					if (!Directory.isDirectory()) {
						Directory.mkdirs();
					}

					String date_str = name.replace(code_pub.get(cur_pub), "");
					date_str = date_str.replace(code_lng, "");
					date_str = date_str.replace("_", "");
					if (date_str.length() == 6)
						date_str += "01";
					SimpleDateFormat format = new SimpleDateFormat("yyyymmdd");
					Date date = null;
					try {
						date = (Date) format.parse(date_str);
					} catch (java.text.ParseException e) {
						Log.e("JWP", e.toString() + " - " + date_str);
					}

					String url_str = URL_IMG;

					if (cur_pub == 1)
						url_str = url_str.replace("{code_pub_shot}", "w");
					else
						url_str = url_str.replace("{code_pub_shot}",
								code_pub.get(cur_pub));
					url_str = url_str.replace("{code_pub}",
							code_pub.get(cur_pub));
					url_str = url_str.replace("{code_lng}", code_lng);
					format.applyPattern("yy");
					url_str = url_str.replace("{YY}", format.format(date));
					if (cur_pub == 2)
						format.applyPattern("yyyymm");
					else
						format.applyPattern("yyyymmdd");
					url_str = url_str
							.replace("{YYYYMMDD}", format.format(date));

					try {
						URL url = new URL(url_str);
						File file = new File(dir, name + ".jpg");
						URLConnection ucon = url.openConnection();
						InputStream is = ucon.getInputStream();
						BufferedInputStream bis = new BufferedInputStream(is);
						ByteArrayBuffer baf = new ByteArrayBuffer(5000);
						int current = 0;
						while ((current = bis.read()) != -1) {
							baf.append((byte) current);
						}

						FileOutputStream fos = new FileOutputStream(file);
						fos.write(baf.toByteArray());
						fos.flush();
						fos.close();

						return true;
					} catch (Exception e) {
						Log.e("JWP_" + getClass().getName(), e.toString());
						return false;
					}
				}
			} catch (Exception e) {
				Log.e("JWP_" + getClass().getName(), e.toString());
			}
			return false;
		}

		protected void onPostExecute(List<class_rss_item> result) {
			this.dialog.hide();
			refresh();
		}

		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(activity, null, activity
					.getResources().getString(R.string.dialog_loaing), true);
		}
	}

}
