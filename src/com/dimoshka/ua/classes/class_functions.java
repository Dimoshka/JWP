package com.dimoshka.ua.classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.dimoshka.ua.jwp.R;

public class class_functions {

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
	public Date get_jwp_rss_date(String name, String code_pub, String code_lng) {
		String date_str = name.replace(code_pub, "");
		date_str = date_str.replace(code_lng, "");
		date_str = date_str.replace("_", "");
		if (date_str.length() == 6)
			date_str += "01";
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		Date date = null;
		try {
			date = (Date) format.parse(date_str);
		} catch (java.text.ParseException e) {
			Log.e("JWP", e.toString() + " - " + date_str);
			return null;
		}
		return date;
	}

	@SuppressLint("SimpleDateFormat")
	public Date get_string_to_date(String date_str, String format_str) {
		SimpleDateFormat format = new SimpleDateFormat(format_str);
		Date date = null;
		try {
			date = (Date) format.parse(date_str);
		} catch (java.text.ParseException e) {
			Log.e("JWP", e.toString() + " - " + date_str);
			return null;
		}
		return date;
	}

	public String get_dir_app(Context context) {
		String dir = Environment.getExternalStorageDirectory() + "/"
				+ context.getResources().getString(R.string.app_dir);
		return dir;
	}

	@SuppressLint("SimpleDateFormat")
	public String getMonth(int month) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
		Calendar localCalendar = Calendar.getInstance();
		localCalendar.set(Calendar.MONTH, month);
		return sdf.format(localCalendar.getTime());
	}
		
}
