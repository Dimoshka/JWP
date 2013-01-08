package com.dimoshka.ua.jwp;

import java.io.File;
import java.util.List;

import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_rss_adapter;
import com.dimoshka.ua.classes.class_rssitem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class main extends Activity {


	ListView list;
	class_rss_adapter adapter;
	List<class_rssitem> rss_list = null;

	// private class_functions funct = new class_functions();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		list = (ListView) findViewById(R.id.list);

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String dir = Environment.getExternalStorageDirectory()
						+ "/JWP/Downloads/";

				File Directory = new File(dir);
				if (!Directory.isDirectory()) {
					Directory.mkdirs();
				}

				Intent i = new Intent(getBaseContext(),
						class_downloads_files.class);

				class_rssitem rss_item = rss_list.get(position);

				i.putExtra("file_url", rss_item.getLink());
				i.putExtra("file_putch", dir + rss_item.getguid());
				startService(i);

			}
		});
	}

	public void onClickStart(View v) {
		// if (class_functions.isNetworkAvailable()==true) {

	//	rss_list = class_rssfeedprovider.parse(URL_FEED);

	//	adapter = new class_rss_adapter(this, rss_list);
	//	list.setAdapter(adapter);

	}

	public void onDestroy() {
		super.onDestroy();
		stopService(new Intent(this, class_downloads_files.class));
	}

}