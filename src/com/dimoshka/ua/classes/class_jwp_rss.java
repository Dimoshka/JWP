package com.dimoshka.ua.classes;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import com.dimoshka.ua.jwp.R;

public class class_jwp_rss {
	static final String URL_FEED = "http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=%s&rmn=%s&rfm=%s&rpf=&rpe=";
	// http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=U&rmn=w&rfm=m4b

	static final String epub = "epub";
	static final String pdf = "pdf";
	static final String aac = "m4b";
	static final String mp3 = "mp3";

	static final String rln = "U";

	static final String w = "w";
	static final String wp = "wp";
	static final String g = "g";

	class_functions funct;
	class_rssfeedprovider rssfeedprovider;
	class_rss_adapter adapter;
	ListView list;

	public void get_all_feeds(Activity activity, ListView list) {
		try {
			this.list = list;
			new ReadFeedTask(activity).execute();
		} catch (Exception e) {
			Log.e("JWP", e.toString());
		}
	}

	public void refresh(Activity activity, List<class_rssitem> rss_list) {
		adapter = new class_rss_adapter(activity, rss_list);
		list.setAdapter(adapter);
	}

	class ReadFeedTask extends AsyncTask<Void, Integer, List<class_rssitem>> {
		private ProgressDialog dialog;
		Activity activity;
		List<class_rssitem> rss_list = null;

		public ReadFeedTask(Activity activity) {
			this.activity = activity;
			Log.e("JWP", "0");
		}

		protected List<class_rssitem> doInBackground(Void... paramArrayOfVoid) {
			try {
				Log.e("JWP", "2");
				// funct = new class_functions();
				// if (funct.isNetworkAvailable() == true) {
				rssfeedprovider = new class_rssfeedprovider();
				this.rss_list = rssfeedprovider.parse(String.format(URL_FEED,
						rln, w, mp3));
				// }
			} catch (Exception e) {
				Log.e("JWP", e.toString());
			}
			return rss_list;
		}

		protected void onPostExecute(List<class_rssitem> result) {
			this.dialog.hide();
			refresh(activity, result);
			Log.e("JWP", "3");
		}

		protected void onPreExecute() {
			Log.e("JWP", "1");
			this.dialog = ProgressDialog.show(activity, null, activity
					.getResources().getString(R.string.dialog_loaing), true);
		}
	}

}
