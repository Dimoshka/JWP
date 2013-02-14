package com.dimoshka.ua.jwp;

import android.os.Bundle;
import android.widget.ExpandableListView;

import com.dimoshka.ua.classes.class_activity_extends;

public class news extends class_activity_extends {

	private ExpandableListView list;
	static final String URL_FEED = "http://www.jw.org/%s/rss/LatestNewsList/feed.xml";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		list = (ExpandableListView) findViewById(R.id.list);

	}

}
