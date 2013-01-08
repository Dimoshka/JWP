package com.dimoshka.ua.jwp;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

public class class_jwp_rsss {
	static final String URL_FEED = "http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=%s&rmn=%s&rfm=%s&rpf=&rpe=";
	// http://www.jw.org/apps/index.xjp?option=sFFZRQVNZNT&rln=U&rmn=w&rfm=m4b

	static final String epub = "epub";
	static final String pdf = "pdg";
	static final String aac = "m4b";
	static final String mp3 = "mp3";

	static final String rln = "U";

	static final String w = "w";
	static final String wp = "wp";
	static final String g = "g";

	List<class_rssitem> rss_list = null;

	private class_functions funct = new class_functions();

	@SuppressLint("ShowToast")
	public void get_all_feeds(Context context) {
		if (funct.isNetworkAvailable() == true) {
			rss_list = class_rssfeedprovider.parse(String.format(URL_FEED, rln,
					w, pdf));

		} else
			Toast.makeText(context, R.string.toast_no_internet,
					Toast.LENGTH_SHORT);
	}

}
