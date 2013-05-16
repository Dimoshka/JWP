package com.dimoshka.ua.classes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.analytics.tracking.android.EasyTracker;

@SuppressLint("Registered")
public class class_activity_extends extends Activity {

	public SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (prefs.getBoolean("analytics", true)) {
			EasyTracker.getInstance().activityStart(this);
		}

	}

	@Override
	public void onStop() {
		super.onStop();

		if (prefs.getBoolean("analytics", true)) {
			EasyTracker.getInstance().activityStop(this);
		}

	}

}
