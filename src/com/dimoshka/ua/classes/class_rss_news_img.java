package com.dimoshka.ua.classes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.http.util.ByteArrayBuffer;

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

public class class_rss_news_img {

	private SQLiteDatabase database;
	public class_functions funct = new class_functions();
	private Activity activity;
	private Cursor cursor;
	private Handler handler;

	public class_rss_news_img(Activity activity, Handler handler,
			SQLiteDatabase database) {
		this.activity = activity;
		this.handler = handler;
		this.database = database;
	}

	public void verify_all_img() {
		cursor = database.rawQuery(
				"select _id, img, link_img from news where img=0", null);
		activity.startManagingCursor(cursor);
		new verify_img().execute();
	}

	class verify_img extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog dialog;
		List<class_rss_item> rss_list = null;

		@SuppressLint("SimpleDateFormat")
		protected Void doInBackground(Void... paramArrayOfVoid) {
			try {
				if (funct.ExternalStorageState()) {
					cursor.moveToFirst();
					String dir = funct.get_dir_app(activity) + "/img/";

					File Directory = new File(dir);
					if (!Directory.isDirectory()) {
						Directory.mkdirs();
					}

					for (int i = 0; i < cursor.getCount(); i++) {
						String name = "news_"
								+ cursor.getString(cursor.getColumnIndex("_id"));
						String link_img = cursor.getString(cursor
								.getColumnIndex("link_img"));
						if (link_img.length() > 0) {

							ContentValues initialValues = new ContentValues();

							File imgFile = new File(dir + "/img/" + name
									+ ".jpg");
							if (!imgFile.exists()) {
								Log.i("JWP_image", name + " - no found!");

								try {
									URL url = new URL(link_img);
									File file = new File(dir, name + ".jpg");
									URLConnection ucon = url.openConnection();
									InputStream is = ucon.getInputStream();
									BufferedInputStream bis = new BufferedInputStream(
											is);
									ByteArrayBuffer baf = new ByteArrayBuffer(
											5000);
									int current = 0;
									while ((current = bis.read()) != -1) {
										baf.append((byte) current);
									}

									FileOutputStream fos = new FileOutputStream(
											file);
									fos.write(baf.toByteArray());
									fos.flush();
									fos.close();

									Log.i("JWP_image", name
											+ " - file download complete!");
									initialValues.put("img", "1");
									String[] args = { String.valueOf(cursor
											.getString(cursor
													.getColumnIndex("_id"))) };
									database.update("news", initialValues,
											"_id=?", args);

								} catch (Exception e) {
									Log.e("JWP_" + getClass().getName(),
											e.toString());
								}
							} else {
								Log.i("JWP_image", name + " found!");
								initialValues.put("img", "1");
								String[] args = { String
										.valueOf(cursor.getString(cursor
												.getColumnIndex("_id"))) };
								database.update("news", initialValues, "_id=?",
										args);
							}
						}
						cursor.moveToNext();
					}
				}
			} catch (Exception e) {
				funct.send_bug_report(activity, e, getClass().getName(), 125);
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			this.dialog.hide();
			activity.stopManagingCursor(cursor);
			cursor.close();
			handler.sendEmptyMessage(2);
		}

		protected void onPreExecute() {
			this.dialog = ProgressDialog
					.show(activity,
							null,
							activity.getResources().getString(
									R.string.dialog_loaing_img), true);
		}
	}

}
