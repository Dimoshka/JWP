package com.dimoshka.ua.classes;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dimoshka.ua.jwp.R;

public class class_rss_adapter extends BaseExpandableListAdapter {
	private Context context;
	public class_functions funct = new class_functions();
	private SQLiteDatabase database;
	private LayoutInflater inflater;

	ArrayList<Map<String, String>> groupData;
	ArrayList<ArrayList<Map<String, String>>> childData;

	public class_rss_adapter(Context context,
			ArrayList<Map<String, String>> groupData,
			ArrayList<ArrayList<Map<String, String>>> childData) {

		this.childData = childData;
		this.groupData = groupData;

		this.context = context;
		class_sqlite dbOpenHelper = new class_sqlite(context,
				context.getString(R.string.db_name), Integer.valueOf(context
						.getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();
	}

	@SuppressLint("SimpleDateFormat")
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.list_items, null);

		Map<String, String> m = getChild(groupPosition, childPosition);
		String name = m.get("name");
		String code_lng = m.get("code_lng");
		String code_pub = m.get("code_pub");
		Date date = funct.get_jwp_rss_date(name, code_pub, code_lng);
		Integer img = Integer.parseInt(m.get("img"));
		Integer _id = Integer.parseInt(m.get("_id"));
		SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");

		TextView title = (TextView) v.findViewById(R.id.title);
		title.setText(format.format(date));

		TextView text = (TextView) v.findViewById(R.id.text);
		text.setText(name);

		ImageView myImage = (ImageView) v.findViewById(R.id.img);
		if (img == 1) {
			File imgFile = new File(funct.get_dir_app(context) + "/img/" + name
					+ ".jpg");
			if (imgFile.exists()) {
				Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
						.getAbsolutePath());
				myImage.setImageBitmap(myBitmap);
			} else {
				myImage.setBackgroundResource(R.drawable.noimages);
				ContentValues initialValues = new ContentValues();
				initialValues.put("img", "0");
				String[] args = { _id.toString() };
				database.update("magazine", initialValues, "_id=?", args);
			}
		} else {
			myImage.setBackgroundResource(R.drawable.noimages);
		}

		return v;
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
		convertView = infalInflater.inflate(
				android.R.layout.simple_expandable_list_item_2, null);

		TextView grouptxt = (TextView) convertView
				.findViewById(android.R.id.text1);
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
