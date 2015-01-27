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
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class class_rss_provider {
    private static final String ITEM = "item";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LINK = "link";
    private static final String PUB_DATE = "pubDate";
    private static final String GUID = "guid";
    private static final String CHANNEL = "channel";
    private class_functions funct;
    private AQuery aq;

    public class_rss_provider(Context context, class_functions funct) {
        this.funct = funct;
        aq = new AQuery(context);
    }

    public List<class_rss_item> parse(String rssFeed) {
        List<class_rss_item> list = new ArrayList<class_rss_item>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            if (funct.isNetworkAvailable()) {
                URL obj = new URL(rssFeed);
                HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                conn.setReadTimeout(5000);
                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                conn.addRequestProperty("User-Agent", "Mozilla");
                //conn.addRequestProperty("Referer", "google.com");
                InputStream stream = null;
                try {
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        stream = conn.getInputStream();
                        parser.setInput(stream, null);
                        int eventType = parser.getEventType();
                        boolean done = false;
                        class_rss_item item = null;
                        while (eventType != XmlPullParser.END_DOCUMENT && !done) {
                            String name;
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
                    }
                } catch (SocketTimeoutException e) {
                    //funct.send_bug_report(e);
                } catch (UnknownHostException e) {
                    //funct.send_bug_report(e);
                } catch (Exception e) {
                    //funct.send_bug_report(e);
                } finally {
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    conn.disconnect();
                }
            }

        } catch (Exception e) {
            funct.send_bug_report(e);
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
                    class_rss_item item;
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
