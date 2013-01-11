package com.dimoshka.ua.classes;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
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
	private SQLiteDatabase database;
	private LayoutInflater vi;
	private int section = 0;
	private int cur_pos = 0;
	private List<Integer> section_arr = new ArrayList<Integer>();

	public class_rss_adapter(Context context, int layout, Cursor cursor,
			String[] from, int[] to) {
		super(context, layout, cursor, from, to);
		this.cursor = cursor;
		this.context = context;
		class_sqlite dbOpenHelper = new class_sqlite(context,
				context.getString(R.string.db_name), Integer.valueOf(context
						.getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();
		vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		int y = 0;
		int m = 0;

		cursor.moveToFirst();
		for (int i = 0; i < cursor.getCount(); i++) {
			String name = this.cursor.getString(this.cursor
					.getColumnIndex("name"));
			String code_lng = this.cursor.getString(this.cursor
					.getColumnIndex("code_lng"));
			String code_pub = this.cursor.getString(this.cursor
					.getColumnIndex("code_pub"));
			Date date = funct.get_jwp_rss_date(name, code_pub, code_lng);

			Log.e("666",
					i + ") " + date.getYear() + "-" + y + "-" + date.getMonth()
							+ "-" + m);

			if (date.getYear() != y || date.getMonth() != m) {
				Log.e("666", "+" + i);
				y = date.getYear();
				m = date.getMonth();
				section++;
				section_arr.add(i);
			} else
				Log.e("666", "-");
			cursor.moveToNext();
		}

	}

	/*
	 * public int getCount() { Log.e("666", section + cursor.getCount() + "");
	 * return section + cursor.getCount() - 1; }
	 */

	@SuppressLint("SimpleDateFormat")
	public View getView(int pos, View inView, ViewGroup parent) {
		View v = inView;
		/*
		 * Log.e("444", pos + ""); if (section_arr.contains(pos)) {
		 * 
		 * Log.e("444","-");
		 * 
		 * v = vi.inflate(R.layout.list_items_section, null);
		 * v.setOnClickListener(null); v.setOnLongClickListener(null);
		 * v.setLongClickable(false);
		 * 
		 * final TextView sectionView = (TextView) v
		 * .findViewById(R.id.list_items_section_text);
		 * sectionView.setText("---");
		 * 
		 * } else { Log.e("444","+ " + cur_pos);
		 */this.cursor.moveToPosition(pos);
		v = vi.inflate(R.layout.list_items, null);
		String name = this.cursor.getString(this.cursor.getColumnIndex("name"));
		String code_lng = this.cursor.getString(this.cursor
				.getColumnIndex("code_lng"));
		String code_pub = this.cursor.getString(this.cursor
				.getColumnIndex("code_pub"));
		Integer cur_pub = cursor.getInt(cursor.getColumnIndex("cur_pub"));
		int img = cursor.getInt(cursor.getColumnIndex("img"));

		SimpleDateFormat format = new SimpleDateFormat("d MMMM yyyy");
		if (cur_pub == 3)
			format.applyPattern("MMMM yyyy");
		Date date = funct.get_jwp_rss_date(name, code_pub, code_lng);
		TextView title = (TextView) v.findViewById(R.id.title);
		title.setText(format.format(date));

		TextView text = (TextView) v.findViewById(R.id.text);
		text.setText(this.cursor.getString(this.cursor.getColumnIndex("name")));

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
				String[] args = { String.valueOf(cursor.getString(cursor
						.getColumnIndex("_id"))) };
				database.update("magazine", initialValues, "_id=?", args);
			}
		} else {
			myImage.setBackgroundResource(R.drawable.noimages);
		}
		// cur_pos++;
		// }

		return (v);
	}

}
