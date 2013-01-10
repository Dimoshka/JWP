package com.dimoshka.ua.classes;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

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
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		} else
			return false;
	}

}
