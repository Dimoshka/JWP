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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_rss_jornals;
import com.dimoshka.ua.classes.class_rss_jornals_adapter;
import com.dimoshka.ua.classes.class_rss_jornals_img;

@SuppressLint("HandlerLeak")
public class jornals extends SherlockFragment {

	private ExpandableListView list;
	private class_rss_jornals rss_jornals;
	private class_rss_jornals_img rss_jornals_img;

	private ArrayList<Map<String, String>> groupData;
	private ArrayList<Map<String, String>> childDataItem;
	private ArrayList<ArrayList<Map<String, String>>> childData;
	private Map<String, String> m;

	int yer = 0;
	int mon = 0;

	private Cursor cursor;
	private Cursor cur_files;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		return inflater.inflate(R.layout.list, group, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		try {

			// prefs =
			// PreferenceManager.getDefaultSharedPreferences(getActivity());
			// id_lang = Integer.parseInt(prefs.getString("language", "0"));

			list = (ExpandableListView) getActivity().findViewById(R.id.list);
			rss_jornals = new class_rss_jornals(getActivity(), main.id_lang,
					handler, main.database);
			main.id_lang = rss_jornals.get_language(main.id_lang);
			rss_jornals_img = new class_rss_jornals_img(getActivity(), handler,
					main.database);

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

		} catch (Exception e) {
			main.funct.send_bug_report(getActivity(), e, getClass().getName(),
					106);
		}

	}

	@SuppressLint("HandlerLeak")
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (main.prefs.getBoolean("downloads_img", true)) {
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
	private void dialog_show(String _id) {
		try {
			if (main.funct.ExternalStorageState() == true) {
				List<String> listItems = new ArrayList<String>();
				CharSequence[] items = null;

				cur_files = main.database
						.rawQuery(
								"select id_type, file, type.name as name_type, files.name, link, files.id_magazine from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
										+ _id + "' group by id_type", null);
				getActivity().startManagingCursor(cur_files);
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

					AlertDialog.Builder builder = new AlertDialog.Builder(
							getActivity());
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
				Toast.makeText(getActivity(), R.string.no_sdcard,
						Toast.LENGTH_SHORT);
		} catch (Exception e) {
			main.funct.send_bug_report(getActivity(), e, getClass().getName(),
					183);
		}
	}

	@SuppressLint("ShowToast")
	private void open_or_download(int id) {
		try {
			if (main.funct.ExternalStorageState() == true) {
				if (cur_files.getCount() > 0) {
					cur_files.moveToPosition(id);
					if (cur_files.getInt(cur_files.getColumnIndex("id_type")) == 3) {
						Intent i = new Intent(getActivity(), player.class);
						i.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
						i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
				Toast.makeText(getActivity(), R.string.no_sdcard,
						Toast.LENGTH_SHORT);
			getActivity().stopManagingCursor(cur_files);
		} catch (Exception e) {
			main.funct.send_bug_report(getActivity(), e, getClass().getName(),
					232);
		}
	}

	private void start_open_or_download(String name, int file_enable,
			String link) {
		try {
			File file = new File(main.funct.get_dir_app(getActivity())
					+ "/downloads/" + name);
			if (file.exists() != true) {
				if (file_enable == 1)
					main.funct.update_file_isn(main.database, name, 0);
				file_enable = 0;
			} else {
				if (file_enable == 0)
					main.funct.update_file_isn(main.database, name, 1);
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
				Intent i = new Intent(getActivity(),
						class_downloads_files.class);
				i.putExtra("file_url", link);
				i.putExtra("file_putch", file.getAbsolutePath());
				Toast.makeText(getActivity(),
						getString(R.string.download_task_addeded),
						Toast.LENGTH_SHORT).show();
				getActivity().startService(i);
			}
		} catch (Exception e) {
			main.funct.send_bug_report(getActivity(), e, getClass().getName(),
					273);
		}
	}

	public void refresh() {
		try {
			getActivity().stopManagingCursor(cursor);
			cursor = main.database
					.rawQuery(
							"select magazine._id as _id, magazine.name as name, magazine.img as img, language.code as code_lng, publication.code as code_pub, publication._id as cur_pub, date from magazine left join language on magazine.id_lang=language._id left join publication on magazine.id_pub=publication._id where magazine.id_lang='"
									+ main.id_lang
									+ "' order by date desc, magazine.id_pub asc",
							null);
			getActivity().startManagingCursor(cursor);

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
				Date date = main.funct.get_jwp_jornals_rss_date(name, code_pub,
						code_lng);

				Cursor cur = main.database.rawQuery(
						"select id_type, file, name from files where `id_magazine`='"
								+ _id + "' group by id_type", null);
				getActivity().startManagingCursor(cur);
				String files = "";
				if (cur.getCount() > 0) {
					cur.moveToFirst();
					for (int a = 0; a < cur.getCount(); a++) {
						if (files.length() > 0)
							files = files + ",";
						int file_isn = 0;
						if (cur.getInt(cur.getColumnIndex("file")) == 1) {
							File file = new File(
									main.funct.get_dir_app(getActivity())
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
								main.database.update("files", initialValues,
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
				getActivity().stopManagingCursor(cur);

				if (date.getYear() != yer || date.getMonth() != mon) {
					yer = date.getYear();
					mon = date.getMonth();
					m = new HashMap<String, String>();
					m.put("groupName", main.funct.getMonth(mon));
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
			getActivity().stopManagingCursor(cursor);
			class_rss_jornals_adapter adapter = new class_rss_jornals_adapter(
					getActivity(), groupData, childData, main.database);
			list.setAdapter(adapter);
		} catch (Exception e) {
			main.funct.send_bug_report(getActivity(), e, getClass().getName(),
					392);
		}
	}

	public void jwp_rss() {
		rss_jornals.get_all_feeds();
	}

	public void jwp_rss_img() {
		rss_jornals_img.verify_all_img();
	}

	@SuppressLint("ShowToast")
	public void load_rss() {
		try {
			if (main.funct.isNetworkAvailable(getActivity()) == true) {
				jwp_rss();
			} else
				Toast.makeText(getActivity(), R.string.no_internet,
						Toast.LENGTH_SHORT);
		} catch (Exception e) {
			Log.e("JWP_" + getClass().getName(), e.toString());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().stopManagingCursor(cursor);
		getActivity().stopManagingCursor(cur_files);
		//dbOpenHelper.close();
		getActivity().stopService(
				new Intent(getActivity(), class_downloads_files.class));

	}

}