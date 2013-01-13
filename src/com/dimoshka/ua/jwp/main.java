package com.dimoshka.ua.jwp;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dimoshka.ua.classes.class_activity_extends;
import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_rss_adapter;
import com.dimoshka.ua.classes.class_rss_jwp;
import com.dimoshka.ua.classes.class_rss_jwp_img;
import com.dimoshka.ua.classes.class_sqlite;

public class main extends class_activity_extends {

	private ExpandableListView list;
	private class_rss_jwp jwp_rss;
	private class_rss_jwp_img jwp_rss_img;

	ArrayList<Map<String, String>> groupData;
	ArrayList<Map<String, String>> childDataItem;
	ArrayList<ArrayList<Map<String, String>>> childData;
	Map<String, String> m;
	String groupFrom[] = new String[] { "groupName" };
	int groupTo[] = new int[] { android.R.id.text1 };
	String childFrom[] = new String[] { "name", "code_pub", "code_lng", "_id",
			"img" };
	int childTo[] = new int[] { R.id.title, R.id.text, R.id.title, R.id.title };

	int yer = 0;
	int mon = 0;

	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Log.e("JWP", "start load image");
				jwp_rss_img();
				break;
			case 2:
				Log.e("JWP", "refrashe afte load");
				refresh();
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		list = (ExpandableListView) findViewById(R.id.list);
		jwp_rss = new class_rss_jwp(this, "ru_RU", handler);
		jwp_rss_img = new class_rss_jwp_img(this, handler);

		class_sqlite dbOpenHelper = new class_sqlite(this,
				getString(R.string.db_name),
				Integer.valueOf(getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();

		refresh();

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String dir = funct.get_dir_app(getBaseContext())
						+ "/downloads/";

				File Directory = new File(dir);
				if (!Directory.isDirectory()) {
					Directory.mkdirs();
				}

				// Intent i = new Intent(getBaseContext(),
				// class_downloads_files.class);

				// class_rss_item rss_item = rss_list.get(position);

				// i.putExtra("file_url", rss_item.getLink());
				// i.putExtra("file_putch", dir + rss_item.getguid());
				// startService(i);

			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 2, Menu.NONE, R.string.m_reload).setIcon(
				android.R.drawable.ic_menu_revert);
		menu.add(Menu.NONE, 1, Menu.NONE, R.string.m_preference).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.m_exit).setIcon(
				android.R.drawable.ic_lock_power_off);

		return true;
	}

	public void refresh() {
		Cursor cursor = database
				.rawQuery(
						"select magazine._id as _id, magazine.name as name, magazine.img as img, language.code as code_lng, publication.code as code_pub, publication._id as cur_pub, date from magazine left join language on magazine.id_lang=language._id left join publication on magazine.id_pub=publication._id order by date desc, magazine.id_pub asc",
						null);
		startManagingCursor(cursor);

		groupData = new ArrayList<Map<String, String>>();

		childData = new ArrayList<ArrayList<Map<String, String>>>();
		childDataItem = new ArrayList<Map<String, String>>();

		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			String name = cursor.getString(cursor.getColumnIndex("name"));
			String code_lng = cursor.getString(cursor
					.getColumnIndex("code_lng"));
			String code_pub = cursor.getString(cursor
					.getColumnIndex("code_pub"));
			Integer img = cursor.getInt(cursor.getColumnIndex("img"));
			Integer _id = cursor.getInt(cursor.getColumnIndex("_id"));
			Date date = funct.get_jwp_rss_date(name, code_pub, code_lng);

			if (date.getYear() != yer || date.getMonth() != mon) {
				yer = date.getYear();
				mon = date.getMonth();
				m = new HashMap<String, String>();
				m.put("groupName", funct.getMonth(mon));
				groupData.add(m);

				if (i > 0) {
					childData.add(childDataItem);
					childDataItem = new ArrayList<Map<String, String>>();
				}

				m = new HashMap<String, String>();
				m.put("name", name);
				m.put("code_pub", code_pub);
				m.put("code_lng", code_lng);
				m.put("_id", _id.toString());
				m.put("img", img.toString());
				childDataItem.add(m);
			} else {
				m = new HashMap<String, String>();
				m.put("name", name);
				m.put("code_pub", code_pub);
				m.put("code_lng", code_lng);
				m.put("_id", _id.toString());
				m.put("img", img.toString());
				childDataItem.add(m);
			}
			cursor.moveToNext();
		}
		childData.add(childDataItem);

		class_rss_adapter adapter = new class_rss_adapter(this, groupData,
				childData);
		list.setAdapter(adapter);
	}

	public void jwp_rss() {
		jwp_rss.get_all_feeds();
	}

	public void jwp_rss_img() {
		jwp_rss_img.verify_all_img();
	}

	@SuppressLint("ShowToast")
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
			try {
				if (funct.isNetworkAvailable(this) == true) {
					jwp_rss();
				} else
					Toast.makeText(this, R.string.toast_no_internet,
							Toast.LENGTH_SHORT);
			} catch (Exception e) {
				Log.e("JWP_" + getClass().getName(), e.toString());
			}
			break;
		default:
			break;
		}

		return false;
	}

	public void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, class_downloads_files.class));
	}

}