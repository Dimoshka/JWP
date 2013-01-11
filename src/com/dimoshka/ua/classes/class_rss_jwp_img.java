package com.dimoshka.ua.classes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
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

public class class_rss_jwp_img {

	static final String URL_IMG = "http://www.jw.org/assets/a/{code_pub_shot}{YY}/{YYYYMMDD}/{code_pub_shot}{YY}_{YYYYMMDD}_{code_lng}/{code_pub}_{code_lng}_{YYYYMMDD}.prd_md.jpg";
	// http://www.jw.org/assets/a/g13/201303/g13_201303_U/g_U_201303prd_md.jpg

	private SQLiteDatabase database;
	public class_functions funct = new class_functions();
	private Activity activity;
	private Cursor cursor;
	private Handler handler;

	public class_rss_jwp_img(Activity activity, Handler handler) {
		this.activity = activity;
		this.handler = handler;
		class_sqlite dbOpenHelper = new class_sqlite(activity,
				activity.getString(R.string.db_name), Integer.valueOf(activity
						.getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();
	}

	public void verify_all_img() {
		cursor = database
				.rawQuery(
						"select magazine._id, magazine.name, magazine.img, language.code as code_lng, publication.code as code_pub, publication._id as cur_pub from magazine left join language on magazine.id_lang=language._id left join publication on magazine.id_pub=publication._id where img=0",
						null);
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
						String name = cursor.getString(cursor
								.getColumnIndex("name"));
						String code_lng = cursor.getString(cursor
								.getColumnIndex("code_lng"));
						String code_pub = cursor.getString(cursor
								.getColumnIndex("code_pub"));
						Integer cur_pub = cursor.getInt(cursor
								.getColumnIndex("cur_pub"));
						ContentValues initialValues = new ContentValues();

						File imgFile = new File(dir + "/img/" + name + ".jpg");
						if (!imgFile.exists()) {
							Log.i("JWP_image", name + "no found!");
							SimpleDateFormat format = new SimpleDateFormat(
									"yyyyMMdd");
							Date date = funct.get_jwp_rss_date(name, code_pub,
									code_lng);

							String url_str = URL_IMG;

							if (cur_pub == 2)
								url_str = url_str.replace("{code_pub_shot}",
										"w");
							else
								url_str = url_str.replace("{code_pub_shot}",
										code_pub);
							url_str = url_str.replace("{code_pub}", code_pub);
							url_str = url_str.replace("{code_lng}", code_lng);
							format.applyPattern("yy");
							url_str = url_str.replace("{YY}",
									format.format(date));
							if (cur_pub == 3)
								format.applyPattern("yyyyMM");
							else
								format.applyPattern("yyyyMMdd");
							url_str = url_str.replace("{YYYYMMDD}",
									format.format(date));

							try {
								URL url = new URL(url_str);
								File file = new File(dir, name + ".jpg");
								URLConnection ucon = url.openConnection();
								InputStream is = ucon.getInputStream();
								BufferedInputStream bis = new BufferedInputStream(
										is);
								ByteArrayBuffer baf = new ByteArrayBuffer(5000);
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
										+ " file download complete!");
								initialValues.put("img", "1");
								String[] args = { String
										.valueOf(cursor.getString(cursor
												.getColumnIndex("_id"))) };
								database.update("magazine", initialValues,
										"_id=?", args);

							} catch (Exception e) {
								Log.e("JWP_" + getClass().getName(),
										e.toString());
							}
						} else {
							Log.i("JWP_image", name + " found!");
							initialValues.put("img", "1");
							String[] args = { String.valueOf(cursor
									.getString(cursor.getColumnIndex("_id"))) };
							database.update("magazine", initialValues, "_id=?",
									args);
						}
						cursor.moveToNext();
					}
					cursor.close();
				}
			} catch (Exception e) {
				Log.e("JWP_" + getClass().getName(), e.toString());
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			this.dialog.hide();
			//onDestroy();
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

	protected void onDestroy() {
		cursor.close();
		database.close();
	}

}
