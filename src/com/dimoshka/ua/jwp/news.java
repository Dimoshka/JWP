package com.dimoshka.ua.jwp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
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
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.dimoshka.ua.classes.class_rss_news;
import com.dimoshka.ua.classes.class_rss_news_adapter;
import com.dimoshka.ua.classes.class_rss_news_img;

@SuppressLint("HandlerLeak")
public class news extends SherlockFragment {

	private ExpandableListView list;
	private class_rss_news rss_news;
	private class_rss_news_img rss_news_img;
	private Cursor cursor;
	private ArrayList<Map<String, String>> groupData;
	private ArrayList<Map<String, String>> childDataItem;
	private ArrayList<ArrayList<Map<String, String>>> childData;
	private Map<String, String> m;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		return inflater.inflate(R.layout.expandable_list, group, false);
	}	
		
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		try {
			list = (ExpandableListView) getActivity().findViewById(R.id.list);
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

			rss_news = new class_rss_news(getActivity(), main.id_lang, handler,
					main.database);
			main.id_lang = rss_news.get_language(main.id_lang);
			rss_news_img = new class_rss_news_img(getActivity(), handler,
					main.database);

		} catch (Exception e) {
			main.funct.send_bug_report(getActivity(), e, getClass().getName(),
					102);
		}
		
		refresh();
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

	@SuppressLint("SimpleDateFormat")
	public void refresh() {
		try {
			getActivity().stopManagingCursor(cursor);
			cursor = main.database.rawQuery(
					"select * from news where news.id_lang='" + main.id_lang
							+ "' order by pubdate desc, news._id asc", null);
			getActivity().startManagingCursor(cursor);

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

			getActivity().stopManagingCursor(cursor);

			class_rss_news_adapter adapter = new class_rss_news_adapter(
					getActivity(), groupData, childData, main.database);
			list.setAdapter(adapter);
		} catch (Exception e) {
			main.funct.send_bug_report(getActivity(), e, getClass().getName(),
					182);
		}
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

	public void jwp_rss() {
		rss_news.get_all_feeds();
	}

	public void jwp_rss_img() {
		rss_news_img.verify_all_img();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().stopManagingCursor(cursor);
		// dbOpenHelper.close();
	}

}
