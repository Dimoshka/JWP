package com.dimoshka.ua.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dimoshka.ua.jwp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class class_rss_news_adapter extends BaseExpandableListAdapter {
	private Context context;
	
    public class_functions funct = new class_functions();
	private LayoutInflater inflater;
	private SQLiteDatabase database;

	ArrayList<Map<String, String>> groupData;
	ArrayList<ArrayList<Map<String, String>>> childData;

	public class_rss_news_adapter(Context context,
			ArrayList<Map<String, String>> groupData,
			ArrayList<ArrayList<Map<String, String>>> childData,
			SQLiteDatabase database) {

		this.childData = childData;
		this.groupData = groupData;
		this.database = database;
		this.context = context;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		try {
			inflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.list_items_news, null);
			Map<String, String> m = getChild(groupPosition, childPosition);

			String description = m.get("description");
			String pubdate = m.get("pubdate");
			Integer img = Integer.parseInt(m.get("img"));
			String name = m.get("name");
			Integer _id = Integer.parseInt(m.get("_id"));

			TextView text = (TextView) v.findViewById(R.id.title);
			text.setText(description);

			TextView date = (TextView) v.findViewById(R.id.text);
			date.setText(pubdate);

			ImageView myImage = (ImageView) v.findViewById(R.id.img);
			if (img == 1) {
				File imgFile = new File(funct.get_dir_app(context) + "/img/"
						+ name + ".jpg");
				if (imgFile.exists()) {
					Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
							.getAbsolutePath());
					myImage.setImageBitmap(myBitmap);
				} else {
					myImage.setImageResource(R.drawable.ic_noimages);
					ContentValues initialValues = new ContentValues();
					initialValues.put("img", "0");
					database.update("news", initialValues, "_id=?",
							new String[] { _id.toString() });
				}
			} else {
				myImage.setImageResource(R.drawable.ic_noimages);
			}

			return v;
		} catch (Exception e) {
			funct.send_bug_report(context, e, getClass().getName(), 29);
			return null;
		}
	}

	@Override
	public Map<String, String> getChild(int groupPosition, int childPosition) {
		return childData.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return childData.get(groupPosition).size();
	}

	@Override
	public Map<String, String> getGroup(int groupPosition) {
		return groupData.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return groupData.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		Map<String, String> m = getGroup(groupPosition);
		LayoutInflater infalInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = infalInflater.inflate(R.layout.list_items_section, null);

		TextView grouptxt = (TextView) convertView.findViewById(R.id.text1);
		grouptxt.setText(m.get("groupName"));
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
