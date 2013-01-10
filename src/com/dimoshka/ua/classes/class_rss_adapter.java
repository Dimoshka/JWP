package com.dimoshka.ua.classes;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dimoshka.ua.jwp.R;

public class class_rss_adapter extends BaseAdapter {
	private Activity activity;
	private List<class_rss_item> data;
	private static LayoutInflater inflater = null;
	static final String img_src = "http://download.jw.org/files/media_magazines/";
	final int stub_id = R.drawable.noimages;

	public class_rss_adapter(Activity a, List<class_rss_item> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return data.size();
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
		//TextView text = (TextView) vi.findViewById(R.id.text);
		ImageView thumb_image = (ImageView) vi.findViewById(R.id.img);

		class_rss_item rss_item = data.get(position);
		title.setText(rss_item.getTitle());
		//text.setText(rss_item.getDescription());

		Log.i("IMG", img_src + rss_item.getguid().replace(".pdf", ".jpg"));

		try {
			Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(
					img_src + rss_item.getguid().replace(".pdf", ".jpg"))
					.getContent());
			thumb_image.setImageBitmap(bitmap);
		} catch (Exception e) {
			e.printStackTrace();
			thumb_image.setImageResource(stub_id);
		}

		return vi;
	}
}
