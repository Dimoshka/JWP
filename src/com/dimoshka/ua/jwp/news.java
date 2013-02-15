package com.dimoshka.ua.jwp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dimoshka.ua.classes.class_activity_extends;
import com.dimoshka.ua.classes.class_rss_news;
import com.dimoshka.ua.classes.class_rss_news_adapter;
import com.dimoshka.ua.classes.class_sqlite;

public class news extends class_activity_extends {

	private ExpandableListView list;
	private class_rss_news rss_news;
	private Cursor cursor;
	private ArrayList<Map<String, String>> groupData;
	private ArrayList<Map<String, String>> childDataItem;
	private ArrayList<ArrayList<Map<String, String>>> childData;
	private Map<String, String> m;
	private class_sqlite dbOpenHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);

		list = (ExpandableListView) findViewById(R.id.list);

		dbOpenHelper = new class_sqlite(this);
		database = dbOpenHelper.openDataBase();

		rss_news = new class_rss_news(this, id_lang, handler, database);

		listener_pref = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs,
					String key) {
				id_lang = Integer.parseInt(prefs.getString("language", "1"));
				rss_news.get_language(id_lang);
				refresh();
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(listener_pref);

		if (prefs.getBoolean("downloads_on_start", false)) {
			load_rss();
		}

		boolean firstrun = prefs.getBoolean("first_run", true);
		if (firstrun) {

			new AlertDialog.Builder(this).setTitle("First Run")
					.setMessage(getString(R.string.first_run))
					.setNeutralButton("OK", null).show();
			prefs.edit().putBoolean("first_run", false).commit();
		} else {
			refresh();
		}

	}

	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Log.e("JWP", "refrashe afte load");
				refresh();
				break;
			}
		}
	};

	private void refresh() {
		stopManagingCursor(cursor);
		cursor = database.rawQuery("select * from news where news.id_lang='"
				+ id_lang + "' order by pubdate desc, news._id asc", null);
		startManagingCursor(cursor);

		groupData = new ArrayList<Map<String, String>>();

		childData = new ArrayList<ArrayList<Map<String, String>>>();
		childDataItem = new ArrayList<Map<String, String>>();

		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {

			childDataItem = new ArrayList<Map<String, String>>();
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String link = cursor.getString(cursor.getColumnIndex("link"));
			String description = cursor.getString(cursor
					.getColumnIndex("description"));
			String pubdate = cursor.getString(cursor.getColumnIndex("pubdate"));

			m = new HashMap<String, String>();
			m.put("groupName", title);
			groupData.add(m);

			m = new HashMap<String, String>();
			m.put("title", title);
			m.put("link", link);
			m.put("description", description);
			m.put("pubdate", pubdate);

			childDataItem.add(m);
			childData.add(childDataItem);
			cursor.moveToNext();
		}

		stopManagingCursor(cursor);

		class_rss_news_adapter adapter = new class_rss_news_adapter(this,
				groupData, childData);
		list.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 3, Menu.NONE, R.string.refrashe).setIcon(
				android.R.drawable.ic_menu_rotate);
		menu.add(Menu.NONE, 2, Menu.NONE, R.string.download_rss).setIcon(
				android.R.drawable.ic_menu_revert);
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

	@SuppressLint("ShowToast")
	private void load_rss() {
		try {
			if (funct.isNetworkAvailable(this) == true) {
				jwp_rss();
			} else
				Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT);
		} catch (Exception e) {
			Log.e("JWP_" + getClass().getName(), e.toString());
		}
	}

	public void jwp_rss() {
		rss_news.get_all_feeds();
	}

}
