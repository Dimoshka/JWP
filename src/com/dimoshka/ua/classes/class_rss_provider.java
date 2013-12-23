package com.dimoshka.ua.classes;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class class_rss_provider {
	static final String ITEM = "item";
	static final String TITLE = "title";
	static final String DESCRIPTION = "description";
	static final String LINK = "link";
	static final String PUB_DATE = "pubDate";
	static final String GUID = "guid";
	static final String CHANNEL = "channel";
	//private class_functions funct = new class_functions();

	
    public List<class_rss_item> parse(String rssFeed, Context context) {
		List<class_rss_item> list = new ArrayList<class_rss_item>();
		XmlPullParser parser = Xml.newPullParser();

		InputStream stream = null;
		try {
			stream = new URL(rssFeed).openConnection().getInputStream();
			parser.setInput(stream, null);

			int eventType = parser.getEventType();
			boolean done = false;
			class_rss_item item = null;
			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equalsIgnoreCase(ITEM)) {
						Log.i("new item", "Create new item");
						item = new class_rss_item();
					} else if (item != null) {
						if (name.equalsIgnoreCase(LINK)) {
							Log.i("Attribute", "setLink");
							item.setLink(parser.nextText());
						} else if (name.equalsIgnoreCase(DESCRIPTION)) {
							Log.i("Attribute", "description");
							item.setDescription(parser.nextText().trim());
						} else if (name.equalsIgnoreCase(PUB_DATE)) {
							Log.i("Attribute", "date");
							item.setPubDate(parser.nextText());
						} else if (name.equalsIgnoreCase(TITLE)) {
							Log.i("Attribute", "title");
							item.setTitle(parser.nextText().trim());
						} else if (name.equalsIgnoreCase(GUID)) {
							Log.i("Attribute", "guid");
							item.setguid(parser.nextText().trim());
						}
					}

					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();
					Log.i("End tag", name);
					if (name.equalsIgnoreCase(ITEM) && item != null) {
						Log.i("Added", item.toString());
						list.add(item);
					} else if (name.equalsIgnoreCase(CHANNEL)) {
						done = true;
					}

					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
		//	funct.send_bug_report(context, e, getClass().getName(), 81);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}
}
