package ua.pp.dimoshka.classes;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.util.XmlDom;

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
    private class_functions funct;
    private AQuery aq;

    public class_rss_provider(Context context, class_functions funct) {
        this.funct = funct;
        aq = new AQuery(context);
    }

    public List<class_rss_item> parse(String rssFeed) {
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
                            Log.d("new item", "Create new item");
                            item = new class_rss_item();
                        } else if (item != null) {
                            if (name.equalsIgnoreCase(LINK)) {
                                Log.d("Attribute", LINK);
                                item.setLink(parser.nextText());
                            } else if (name.equalsIgnoreCase(DESCRIPTION)) {
                                Log.d("Attribute", DESCRIPTION);
                                item.setDescription(parser.nextText().trim());
                            } else if (name.equalsIgnoreCase(PUB_DATE)) {
                                Log.d("Attribute", PUB_DATE);
                                item.setPubDate(parser.nextText());
                            } else if (name.equalsIgnoreCase(TITLE)) {
                                Log.d("Attribute", TITLE);
                                item.setTitle(parser.nextText().trim());
                            } else if (name.equalsIgnoreCase(GUID)) {
                                Log.d("Attribute", GUID);
                                item.setguid(parser.nextText().trim());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        Log.d("End tag", name);
                        if (name.equalsIgnoreCase(ITEM) && item != null) {
                            Log.d("Added", item.toString());
                            list.add(item);
                        } else if (name.equalsIgnoreCase(CHANNEL)) {
                            done = true;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
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


    public List<class_rss_item> parse_aq(String rssFeed) {
        final List<class_rss_item> list = new ArrayList<class_rss_item>();
        try {
            aq.ajax(rssFeed, XmlDom.class, new AjaxCallback<XmlDom>() {
                @Override
                public void callback(String url, XmlDom xml, AjaxStatus status) {
                    List<XmlDom> entries = xml.tags(ITEM);
                    //List<class_rss_item> list = new ArrayList<class_rss_item>();
                    class_rss_item item = null;
                    for (XmlDom entry : entries) {
                        item = new class_rss_item();

                        Log.d("RSS Attribute", LINK);
                        item.setLink(entry.text(LINK).trim());
                        Log.d("RSS Attribute", DESCRIPTION);
                        item.setDescription(entry.text(DESCRIPTION).trim());
                        Log.d("RSS Attribute", PUB_DATE);
                        item.setPubDate(entry.text(PUB_DATE).trim());
                        Log.d("RSS Attribute", TITLE);
                        item.setTitle(entry.text(TITLE).trim());
                        Log.d("RSS Attribute", GUID);
                        item.setguid(entry.text(GUID).trim());
                        Log.d("RSS Added", item.toString());
                        list.add(item);
                    }
                }
            });
        } catch (Exception e) {
            funct.send_bug_report(e);
        }

        return list;
    }
}
