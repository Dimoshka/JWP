package com.dimoshka.ua.classes;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

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

	List<class_rssitem> rss_list = null;
	class_functions funct = new class_functions();
	class_rssfeedprovider rssfeedprovider;

	private rssfeed_parse rp;

	public List<class_rssitem> get_all_feeds(Activity activity) {
		try {
			rss_list = null;
			rp = new rssfeed_parse(activity);
			rp.execute(String.format(URL_FEED, rln, w, pdf));
			rss_list = rp.get();

			return rss_list;
		} catch (Exception e) {
			Log.e("JWP", e.toString());
			return rss_list;
		}
	}

	public class rssfeed_parse extends
			AsyncTask<String, Void, List<class_rssitem>> {

		private ProgressDialog dialog;
		Activity activity;

		protected rssfeed_parse(Activity activity) {
			this.activity = activity;
		}

		protected void onPreExecute() {
			dialog = new ProgressDialog(activity);
			dialog.setMessage("Processing...");
			dialog.show();
		}

		protected void onPostExecute(Void result) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		};

		protected List<class_rssitem> doInBackground(String... url) {
			// if (funct.isNetworkAvailable() == true) {
			Log.e("JWP", "Online");
			rssfeedprovider = new class_rssfeedprovider();
			rss_list = rssfeedprovider.parse(url[0]);
			// } else
			Log.e("JWP", "Offline");
			return rss_list;
		}
	}

}
