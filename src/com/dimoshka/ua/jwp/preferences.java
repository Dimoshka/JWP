package com.dimoshka.ua.jwp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.google.analytics.tracking.android.EasyTracker;

public class preferences extends SherlockPreferenceActivity {

    private ActionBar actionBar;
    private static SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.jwp);
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background_textured_jwp));
        PreferenceManager.setDefaultValues(preferences.this, R.xml.preferences, true);
        //getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (prefs.getBoolean("analytics", true)) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (prefs.getBoolean("analytics", true)) {
            EasyTracker.getInstance(this).activityStop(this);
        }
    }

}
