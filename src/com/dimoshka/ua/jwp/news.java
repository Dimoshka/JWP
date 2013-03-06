package com.dimoshka.ua.jwp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dimoshka.ua.classes.class_activity_extends;
import com.dimoshka.ua.classes.class_rss_news;
import com.dimoshka.ua.classes.class_rss_news_adapter;
import com.dimoshka.ua.classes.class_rss_news_img;
import com.dimoshka.ua.classes.class_sqlite;

@SuppressLint("HandlerLeak")
public class news extends class_activity_extends {

	private ExpandableListView list;
	private class_rss_news rss_news;
	private class_rss_news_img rss_news_img;
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
		try {
			list = (ExpandableListView) findViewById(R.id.list);

			dbOpenHelper = new class_sqlite(this);
			database = dbOpenHelper.openDataBase();

			list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView arg0, View arg1,
						int arg2, int arg3, long arg4) {
					Map<String, String> hash = new HashMap<String, String>();
					hash = childData.get(arg2).get(arg3);

					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);

					Uri data = Uri.parse(hash.get("link"));
					intent.setDataAndType(data, "text/html");
					Intent ch = Intent.createChooser(intent,
							getString(R.string.select));
					startActivity(ch);
					return false;
				}
			});

			rss_news = new class_rss_news(this, id_lang, handler, database);
			id_lang = rss_news.get_language(id_lang);
			rss_news_img = new class_rss_news_img(this, handler, database);

			listener_pref = new SharedPreferences.OnSharedPreferenceChangeListener() {
				public void onSharedPreferenceChanged(SharedPreferences prefs,
						String key) {
					id_lang = Integer
							.parseInt(prefs.getString("language", "0"));
					id_lang = rss_news.get_language(id_lang);
					refresh();
				}
			};
			prefs.registerOnSharedPreferenceChangeListener(listener_pref);

			if (prefs.getBoolean("downloads_on_start", false)) {
				load_rss();
			}

			boolean firstrun = prefs.getBoolean("first_run", true);
			if (firstrun) {
				new AlertDialog.Builder(this)
						.setTitle(getString(R.string.first_run_title))
						.setMessage(getString(R.string.first_run))
						.setNeutralButton("OK", null).show();
				prefs.edit().putBoolean("first_run", false).commit();
			} else {
				refresh();
			}

		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					102);
		}
	}

	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (prefs.getBoolean("downloads_img", true)) {
					Log.e("JWP", "start load image");
					jwp_rss_img();
				} else {
					refresh();
				}
				break;
			case 2:
				Log.e("JWP", "refrashe afte load");
				refresh();
				break;
			}
		}

	};

	@SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	private void refresh() {
		try {
			stopManagingCursor(cursor);
			cursor = database.rawQuery(
					"select * from news where news.id_lang='" + id_lang
							+ "' order by pubdate desc, news._id asc", null);
			startManagingCursor(cursor);

			groupData = new ArrayList<Map<String, String>>();

			childData = new ArrayList<ArrayList<Map<String, String>>>();
			childDataItem = new ArrayList<Map<String, String>>();

			cursor.moveToFirst();
			for (int i = 0; i < cursor.getCount(); i++) {

				childDataItem = new ArrayList<Map<String, String>>();

				Integer _id = cursor.getInt(cursor.getColumnIndex("_id"));
				String title = cursor.getString(cursor.getColumnIndex("title"));
				String link = cursor.getString(cursor.getColumnIndex("link"));
				String description = cursor.getString(cursor
						.getColumnIndex("description"));
				String pubdate = cursor.getString(cursor
						.getColumnIndex("pubdate"));

				Integer img = cursor.getInt(cursor.getColumnIndex("img"));

				m = new HashMap<String, String>();
				m.put("groupName", title);
				groupData.add(m);

				m = new HashMap<String, String>();

				m.put("_id", _id.toString());
				m.put("name", "news_" + _id.toString());
				m.put("title", title);
				m.put("link", link);
				m.put("description", description);
				m.put("pubdate", pubdate);
				m.put("img", img.toString());

				childDataItem.add(m);
				childData.add(childDataItem);
				cursor.moveToNext();
			}

			stopManagingCursor(cursor);

			class_rss_news_adapter adapter = new class_rss_news_adapter(this,
					groupData, childData, database);
			list.setAdapter(adapter);
		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					182);
		}
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

	public void jwp_rss_img() {
		rss_news_img.verify_all_img();
	}

	@SuppressWarnings("deprecation")
	public void onDestroy() {
		super.onDestroy();
		stopManagingCursor(cursor);
		dbOpenHelper.close();
	}

}
