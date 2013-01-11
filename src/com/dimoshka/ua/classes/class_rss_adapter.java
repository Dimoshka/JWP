package com.dimoshka.ua.classes;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.dimoshka.ua.jwp.R;

public class class_rss_adapter extends SimpleCursorAdapter {
	private Cursor cursor;
	private Context context;
	public class_functions funct = new class_functions();

	public class_rss_adapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		this.cursor = cursor;
		this.context = context;
	}

	@SuppressLint("SimpleDateFormat")
	public View getView(int pos, View inView, ViewGroup parent) {
		View v = inView;
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_items, null);
		}
		this.cursor.moveToPosition(pos);

		String name = this.cursor.getString(this.cursor.getColumnIndex("name"));
		String code_lng = this.cursor.getString(this.cursor
				.getColumnIndex("code_lng"));
		String code_pub = this.cursor.getString(this.cursor
				.getColumnIndex("code_pub"));
		Integer cur_pub = cursor.getInt(cursor.getColumnIndex("cur_pub"));

		SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");
		if (cur_pub == 3)
			format.applyPattern("MMMM yyyy");
		Date date = funct.get_jwp_rss_date(name, code_pub, code_lng);
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setText(format.format(date));

		TextView text = (TextView) v.findViewById(R.id.text);
		text.setText(this.cursor.getString(this.cursor.getColumnIndex("name")));

		ImageView iv = (ImageView) v.findViewById(R.id.img);

		return (v);
	}

}
