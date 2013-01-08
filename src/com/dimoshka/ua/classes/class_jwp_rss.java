package com.dimoshka.ua.classes;

import java.util.List;
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
	class_rssfeedprovider rssfeedprovider = new class_rssfeedprovider();

	public void get_all() {

	}

	public List<class_rssitem> get_all_feeds() {
		try {
			rss_list = null;
			Log.e("RSS", "3");
			//if (funct.isNetworkAvailable() == true) {
				Log.e("RSS", "1");
				rss_list = rssfeedprovider.parse(String.format(URL_FEED, rln,
						w, pdf));
			//} else Log.e("RSS", "0");
			return rss_list;
		} catch (Exception e) {
			Log.e("RSS", e.toString());
			return rss_list;
		}
	}
}
