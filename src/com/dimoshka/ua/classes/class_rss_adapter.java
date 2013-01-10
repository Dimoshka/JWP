package com.dimoshka.ua.classes;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.dimoshka.ua.jwp.R;

public class class_rss_adapter extends BaseAdapter {
	private Activity activity;
	private static LayoutInflater inflater = null;
	final int stub_id = R.drawable.noimages;
	private SQLiteDatabase database;
	private Cursor cursor;

	public class_rss_adapter(Activity a) {
		activity = a;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		class_sqlite dbOpenHelper = new class_sqlite(a,
				a.getString(R.string.db_name), Integer.valueOf(a
						.getString(R.string.db_version)));
		database = dbOpenHelper.openDataBase();

		cursor = database.query("magazine", new String[] { "_id", "name",
				"img", "id_lang" }, null, null, null, null, "_id");

	}

	public int getCount() {
		return cursor.getCount();
	}

	public Object getItem(int position) {
		return position;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_items, null);

		TextView title = (TextView) vi.findViewById(R.id.title);
		// TextView text = (TextView) vi.findViewById(R.id.text);
		ImageView thumb_image = (ImageView) vi.findViewById(R.id.img);

		cursor.moveToPosition(position);

		title.setText(cursor.getString(cursor.getColumnIndex("name")));
		// text.setText(rss_item.getDescription());

		// Log.i("IMG", img_src + rss_item.getguid().replace(".pdf", ".jpg"));

		try {
			// Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(
			// img_src + rss_item.getguid().replace(".pdf", ".jpg"))
			// .getContent());
			// thumb_image.setImageBitmap(bitmap);
		} catch (Exception e) {
			e.printStackTrace();
			thumb_image.setImageResource(stub_id);
		}

		return vi;
	}
}
