package com.dimoshka.ua.jwp;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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
import android.webkit.MimeTypeMap;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.dimoshka.ua.classes.class_activity_extends;
import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_rss_jornals_adapter;
import com.dimoshka.ua.classes.class_rss_jornals;
import com.dimoshka.ua.classes.class_rss_jornals_img;
import com.dimoshka.ua.classes.class_sqlite;

@SuppressLint("HandlerLeak")
public class jornals extends class_activity_extends {

	private ExpandableListView list;
	private class_rss_jornals rss_jornals;
	private class_rss_jornals_img rss_jornals_img;

	private ArrayList<Map<String, String>> groupData;
	private ArrayList<Map<String, String>> childDataItem;
	private ArrayList<ArrayList<Map<String, String>>> childData;
	private Map<String, String> m;
	private class_sqlite dbOpenHelper;

	int yer = 0;
	int mon = 0;

	private Cursor cursor;
	private Cursor cur_files;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.list);
			list = (ExpandableListView) findViewById(R.id.list);

			dbOpenHelper = new class_sqlite(this);
			database = dbOpenHelper.openDataBase();

			rss_jornals = new class_rss_jornals(this, id_lang, handler,
					database);
			id_lang = rss_jornals.get_language(id_lang);
			rss_jornals_img = new class_rss_jornals_img(this, handler, database);

			list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView arg0, View arg1,
						int arg2, int arg3, long arg4) {
					Map<String, String> hash = new HashMap<String, String>();
					hash = childData.get(arg2).get(arg3);
					dialog_show(hash.get("_id"));
					return false;
				}
			});

			listener_pref = new SharedPreferences.OnSharedPreferenceChangeListener() {
				public void onSharedPreferenceChanged(SharedPreferences prefs,
						String key) {
					id_lang = Integer
							.parseInt(prefs.getString("language", "0"));
					id_lang = rss_jornals.get_language(id_lang);
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
					106);
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

	@SuppressLint("ShowToast")
	@SuppressWarnings("deprecation")
	private void dialog_show(String _id) {
		try {
			if (funct.ExternalStorageState() == true) {
				List<String> listItems = new ArrayList<String>();
				CharSequence[] items = null;

				cur_files = database
						.rawQuery(
								"select id_type, file, type.name as name_type, files.name, link, files.id_magazine from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
										+ _id + "' group by id_type", null);
				startManagingCursor(cur_files);
				if (cur_files.getCount() > 0) {
					cur_files.moveToFirst();
					for (int i = 0; i < cur_files.getCount(); i++) {
						String name = null;

						if (cur_files.getInt(cur_files
								.getColumnIndex("id_type")) != 3) {
							if (cur_files.getInt(cur_files
									.getColumnIndex("file")) == 1) {
								name = getString(R.string.open)
										+ " "
										+ cur_files.getString(cur_files
												.getColumnIndex("name_type"));
							} else {
								name = getString(R.string.download)
										+ " "
										+ cur_files.getString(cur_files
												.getColumnIndex("name_type"));
							}
						} else {
							name = getString(R.string.player_open)
									+ " ("
									+ cur_files.getString(cur_files
											.getColumnIndex("name_type")) + ")";
						}
						listItems.add(name);
						cur_files.moveToNext();
					}

					items = listItems
							.toArray(new CharSequence[listItems.size()]);

					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(getString(R.string.select_the_action));
					builder.setItems(items,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int item) {
									open_or_download(item);
								}
							});
					AlertDialog alert = builder.create();
					alert.show();
				}
			} else
				Toast.makeText(this, R.string.no_sdcard, Toast.LENGTH_SHORT);
		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					183);
		}
	}

	@SuppressLint("ShowToast")
	@SuppressWarnings("deprecation")
	private void open_or_download(int id) {
		try {
			if (funct.ExternalStorageState() == true) {
				if (cur_files.getCount() > 0) {
					cur_files.moveToPosition(id);
					if (cur_files.getInt(cur_files.getColumnIndex("id_type")) == 3) {
						Intent i = new Intent(this, player.class);
						i.putExtra("id_magazine", cur_files.getInt(cur_files
								.getColumnIndex("id_magazine")));
						startActivity(i);
					} else {
						start_open_or_download(cur_files.getString(cur_files
								.getColumnIndex("name")),
								cur_files.getInt(cur_files
										.getColumnIndex("file")),
								cur_files.getString(cur_files
										.getColumnIndex("link")));
					}
				}
			} else
				Toast.makeText(getBaseContext(), R.string.no_sdcard,
						Toast.LENGTH_SHORT);
			stopManagingCursor(cur_files);
		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					232);
		}
	}

	private void start_open_or_download(String name, int file_enable,
			String link) {
		try {
			File file = new File(funct.get_dir_app(this) + "/downloads/" + name);
			if (file.exists() != true) {
				if (file_enable == 1)
					funct.update_file_isn(database, name, 0);
				file_enable = 0;
			} else {
				if (file_enable == 0)
					funct.update_file_isn(database, name, 1);
				file_enable = 1;
			}

			if (file_enable == 1) {
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);

				MimeTypeMap map = MimeTypeMap.getSingleton();
				String ext = MimeTypeMap
						.getFileExtensionFromUrl(file.getName());
				String type = map.getMimeTypeFromExtension(ext);

				if (type == null)
					type = "*/*";
				Uri data = Uri.fromFile(file);
				intent.setDataAndType(data, type);
				Intent ch = Intent.createChooser(intent,
						getString(R.string.select));
				startActivity(ch);

			} else {
				Intent i = new Intent(getBaseContext(),
						class_downloads_files.class);
				i.putExtra("file_url", link);
				i.putExtra("file_putch", file.getAbsolutePath());
				Toast.makeText(this, getString(R.string.download_task_addeded),
						Toast.LENGTH_SHORT).show();
				startService(i);
			}
		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					273);
		}
	}

	@SuppressWarnings("deprecation")
	private void refresh() {
		try {
			stopManagingCursor(cursor);
			cursor = database
					.rawQuery(
							"select magazine._id as _id, magazine.name as name, magazine.img as img, language.code as code_lng, publication.code as code_pub, publication._id as cur_pub, date from magazine left join language on magazine.id_lang=language._id left join publication on magazine.id_pub=publication._id where magazine.id_lang='"
									+ id_lang
									+ "' order by date desc, magazine.id_pub asc",
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
				Date date = funct.get_jwp_jornals_rss_date(name, code_pub,
						code_lng);

				Cursor cur = database.rawQuery(
						"select id_type, file, name from files where `id_magazine`='"
								+ _id + "' group by id_type", null);
				startManagingCursor(cur);
				String files = "";
				if (cur.getCount() > 0) {
					cur.moveToFirst();
					for (int a = 0; a < cur.getCount(); a++) {
						if (files.length() > 0)
							files = files + ",";
						int file_isn = 0;
						if (cur.getInt(cur.getColumnIndex("file")) == 1) {
							File file = new File(
									funct.get_dir_app(getBaseContext())
											+ "/downloads/"
											+ cur.getString(cur
													.getColumnIndex("name")));

							Log.d("JWP" + getClass().getName(),
									cur.getString(cur.getColumnIndex("name")));

							if (file.exists()) {
								file_isn = 1;
							} else {
								Log.d("JWP" + getClass().getName(),
										"Update to 0 - "
												+ cur.getString(cur
														.getColumnIndex("name")));
								ContentValues initialValues = new ContentValues();
								initialValues.put("file", "0");
								database.update("files", initialValues,
										"name=?",
										new String[] { cur.getString(cur
												.getColumnIndex("name")) });
							}
						}

						files = files
								+ cur.getString(cur.getColumnIndex("id_type"))
								+ "=" + file_isn;
						cur.moveToNext();
					}
				}
				stopManagingCursor(cur);

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
					m.put("id_type", files);
					childDataItem.add(m);
				} else {
					m = new HashMap<String, String>();
					m.put("name", name);
					m.put("code_pub", code_pub);
					m.put("code_lng", code_lng);
					m.put("_id", _id.toString());
					m.put("img", img.toString());
					m.put("id_type", files);
					childDataItem.add(m);
				}
				cursor.moveToNext();
			}
			childData.add(childDataItem);
			stopManagingCursor(cursor);
			class_rss_jornals_adapter adapter = new class_rss_jornals_adapter(
					this, groupData, childData, database);
			list.setAdapter(adapter);
		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					392);
		}
	}

	public void jwp_rss() {
		rss_jornals.get_all_feeds();
	}

	public void jwp_rss_img() {
		rss_jornals_img.verify_all_img();
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

	@SuppressWarnings("deprecation")
	public void onDestroy() {
		super.onDestroy();
		stopManagingCursor(cursor);
		stopManagingCursor(cur_files);
		dbOpenHelper.close();
		stopService(new Intent(this, class_downloads_files.class));

	}

}