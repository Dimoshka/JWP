package com.dimoshka.ua.jwp;

import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class preferences extends SherlockPreferenceActivity {

    private ActionBar actionBar;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.orange));


        PreferenceManager.setDefaultValues(preferences.this, R.xml.preferences,
                true);
    }

}
