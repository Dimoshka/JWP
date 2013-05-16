package com.dimoshka.ua.jwp;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dimoshka.ua.classes.class_functions;
import com.dimoshka.ua.classes.class_sqlite;
import com.google.analytics.tracking.android.EasyTracker;

public class main extends SherlockFragmentActivity {

	private int curent_tab = 0;

	public static int id_lang = 0;
	public static SharedPreferences prefs;
	public static SQLiteDatabase database;
	public static class_sqlite dbOpenHelper;
	public static class_functions funct = new class_functions();
	public OnSharedPreferenceChangeListener listener_pref;

	jornals frag1;
	news frag2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		dbOpenHelper = new class_sqlite(this);
		database = dbOpenHelper.openDataBase();

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		id_lang = Integer.parseInt(prefs.getString("language", "0"));

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setTitle(R.string.app_name_shot);

		ActionBar.Tab jornals_Tab = actionBar.newTab()
				.setText(R.string.jornals);
		ActionBar.Tab newsTab = actionBar.newTab().setText(R.string.news);

		jornals_Tab.setTabListener(new MyTabListener());
		newsTab.setTabListener(new MyTabListener());

		frag1 = new jornals();
		frag2 = new news();

		actionBar.addTab(jornals_Tab);
		actionBar.addTab(newsTab);

		boolean firstrun = prefs.getBoolean("first_run", true);
		if (firstrun) {
			new AlertDialog.Builder(this)
					.setTitle(getString(R.string.first_run_title))
					.setMessage(getString(R.string.first_run))
					.setNeutralButton("OK", null).show();
			prefs.edit().putBoolean("first_run", false).commit();
		} else if (prefs.getBoolean("downloads_on_start", false)) {
			//load_rss();
		}

		load_rss();
		
		
		listener_pref = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs,
					String key) {
				id_lang = Integer.parseInt(prefs.getString("language", "0"));
				// id_lang = rss_jornals.get_language(id_lang);
				//refresh();
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(listener_pref);
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

	private class MyTabListener implements ActionBar.TabListener {
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			curent_tab = tab.getPosition();
			if (curent_tab == 0) {
				ft.replace(R.id.fragment_container, frag1);
			} else {
				ft.replace(R.id.fragment_container, frag2);
			}
			//refresh();
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (curent_tab == 0) {
				ft.remove(frag1);
			} else {
				ft.remove(frag2);
			}
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			refresh();
			//ACRA.getErrorReporter().handleSilentException(null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 3, Menu.NONE, R.string.refrashe).setIcon(
				android.R.drawable.ic_menu_rotate);
		menu.add(Menu.NONE, 2, Menu.NONE, R.string.download_rss).setIcon(
				android.R.drawable.ic_menu_revert).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.add(Menu.NONE, 1, Menu.NONE, R.string.preference).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.exit).setIcon(
				android.R.drawable.ic_lock_power_off);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			System.exit(0);
			break;
		case 1:
			Intent i = new Intent(this, preferences.class);
			startActivity(i);
			break;
		case 2:
			load_rss();
			break;
		case 3:
			refresh();
			break;
		default:
			break;
		}

		return false;
	}

	private void load_rss() {
		try {
			if (curent_tab == 0) {
				frag1.load_rss();
			} else {
				frag2.load_rss();
			}
		} catch (Exception e) {
			funct.send_bug_report(this, e, getClass().getName(), 148);
		}
	}

	private void refresh() {
		try {
			if (curent_tab == 0) {
				frag1.refresh();
			} else {
				frag2.refresh();
			}
		} catch (Exception e) {
			funct.send_bug_report(this, e, getClass().getName(), 157);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		dbOpenHelper.close();
	}

}