package com.dimoshka.ua.classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.dimoshka.ua.jwp.R;

public class class_functions {
	public SharedPreferences prefs;

	public boolean isNetworkAvailable(Activity activity) {
		try {
			ConnectivityManager cm = (ConnectivityManager) activity
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				return true;
			}

		} catch (Exception e) {
			Log.e("JWP_" + getClass().getName(), e.toString());
		}
		return false;
	}

	public boolean ExternalStorageState() {
		try {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				return true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				return false;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressLint("SimpleDateFormat")
	public Date get_jwp_jornals_rss_date(String name, String code_pub,
			String code_lng) {
		String date_str = name.replace(code_pub, "");
		date_str = date_str.replace(code_lng, "");
		date_str = date_str.replace("_", "");
		if (date_str.length() == 6)
			date_str += "01";
		return get_string_to_date(date_str, "yyyyMMdd");
	}

	public Date get_string_to_date(String date_str, String format_str) {
		SimpleDateFormat format = new SimpleDateFormat(format_str,
				Locale.ENGLISH);
		Date date = null;
		try {
			date = (Date) format.parse(date_str);
		} catch (java.text.ParseException e) {
			Log.e("JWP", e.toString() + " - " + date_str + " - " + format_str);
			return null;
		}
		return date;
	}

	public String get_dir_app(Context context) {
		String dir = Environment.getExternalStorageDirectory() + "/"
				+ context.getResources().getString(R.string.app_dir);
		return dir;
	}

	public String getMonth(int month) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM", Locale.getDefault());
		Calendar localCalendar = Calendar.getInstance();
		localCalendar.set(Calendar.MONTH, month);
		return sdf.format(localCalendar.getTime());
	}

	public Cursor get_language(SQLiteDatabase database, int id,
			Activity activity) {
		Cursor cursor;
		if (id == 0) {
			cursor = database.rawQuery("SELECT * from language where code_an='"
					+ Locale.getDefault().getLanguage() + "'", null);
			cursor.moveToFirst();
			if (cursor.getCount() > 0) {
				prefs = PreferenceManager.getDefaultSharedPreferences(activity);
				prefs.edit()
						.putString("language",
								cursor.getString(cursor.getColumnIndex("_id")))
						.commit();
			}
		} else {
			cursor = database.rawQuery("SELECT* from language where _id='" + id
					+ "'", null);
		}
		return cursor;
	}

	public void send_bug_report(Context context, Exception ex,
			String class_name, Integer num_row) {

		Log.e(context.getString(R.string.app_name) + " - error " + class_name,
				ex.toString());

		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getBoolean("analytics", true)) {

			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_EMAIL, new String[] { context
					.getString(R.string.app_bug_report_mail) });
			PackageInfo pInfo = null;
			try {
				pInfo = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
			} catch (NameNotFoundException e1) {
				e1.printStackTrace();
			}

			i.putExtra(Intent.EXTRA_SUBJECT,
					"ERROR reporting - " + context.getString(R.string.app_name)
							+ " " + pInfo.versionName);
			i.putExtra(Intent.EXTRA_TEXT, ex.getMessage() + "  -  "
					+ class_name + ":" + num_row.toString());

			try {
				context.startActivity(Intent.createChooser(i,
						context.getString(R.string.send_bug_report)));
			} catch (android.content.ActivityNotFoundException e) {
				Toast.makeText(context,
						context.getString(R.string.not_email_client),
						Toast.LENGTH_SHORT).show();
			}
		}

	}

}
