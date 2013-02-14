package com.dimoshka.ua.jwp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dimoshka.ua.classes.class_activity_extends;
import com.dimoshka.ua.classes.class_rss_news;

public class news extends class_activity_extends {

	private ExpandableListView list;
	private class_rss_news rss_news;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		list = (ExpandableListView) findViewById(R.id.list);

	}

	

	private void refresh() {


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
