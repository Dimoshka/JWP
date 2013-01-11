package com.dimoshka.ua.jwp;

import java.io.File;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dimoshka.ua.classes.class_activity_extends;
import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_rss_adapter;
import com.dimoshka.ua.classes.class_rss_item;
import com.dimoshka.ua.classes.class_rss_jwp;
import com.dimoshka.ua.classes.class_rss_jwp_img;
import com.dimoshka.ua.classes.class_sqlite;

public class main extends class_activity_extends {

	private ListView list;
	private List<class_rss_item> rss_list = null;
	private class_rss_jwp jwp_rss;
	private class_rss_jwp_img jwp_rss_img;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		list = (ListView) findViewById(R.id.list);
		jwp_rss = new class_rss_jwp(this, "ru_RU");
		jwp_rss_img = new class_rss_jwp_img(this);

		class_sqlite dbOpenHelper = new class_sqlite(this,
				getString(R.string.db_name),
				Integer.valueOf(getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();

		refresh();

		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String dir = Environment.getExternalStorageDirectory()
						+ "/JWP/Downloads/";

				File Directory = new File(dir);
				if (!Directory.isDirectory()) {
					Directory.mkdirs();
				}

				Intent i = new Intent(getBaseContext(),
						class_downloads_files.class);

				class_rss_item rss_item = rss_list.get(position);

				i.putExtra("file_url", rss_item.getLink());
				i.putExtra("file_putch", dir + rss_item.getguid());
				startService(i);

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

		BaseAdapter adapter = new class_rss_adapter(this, R.layout.list_items,
				cursor, new String[] { "name", "img" }, new int[] { R.id.title,
						R.id.text });

		list.setAdapter(adapter);

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
					jwp_rss.get_all_feeds(list);
					jwp_rss_img.verify_all_img();
					refresh();
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