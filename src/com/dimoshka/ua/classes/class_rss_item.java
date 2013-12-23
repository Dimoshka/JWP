package com.dimoshka.ua.classes;

public class class_rss_item {
    private String pubDate;
    private String description;
    private String link;
    private String title;
    private String guid;

    public class_rss_item() {
    }

    public class_rss_item(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getguid() {
        return guid;
    }

    public void setguid(String guid) {
        this.guid = guid;
    }

    @Override
    public String toString() {
        return "RssItem [title=" + title + "]";
    }

}
