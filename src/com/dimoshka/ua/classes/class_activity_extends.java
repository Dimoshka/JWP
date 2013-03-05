package com.dimoshka.ua.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.EasyTracker;

@SuppressLint("Registered")
public class class_activity_extends extends Activity {

	public SharedPreferences prefs;
	public SQLiteDatabase database;
	public class_functions funct = new class_functions();
	public int id_lang = 0;
	public OnSharedPreferenceChangeListener listener_pref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			prefs = PreferenceManager.getDefaultSharedPreferences(this);
			id_lang = Integer.parseInt(prefs.getString("language", "0"));

		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(), 29);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		try {
			if (prefs.getBoolean("analytics", true)) {
				EasyTracker.getInstance().activityStart(this);
			}
		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(), 41);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		try {
			if (prefs.getBoolean("analytics", true)) {
				EasyTracker.getInstance().activityStop(this);
			}
		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(), 53);
		}
	}

}
