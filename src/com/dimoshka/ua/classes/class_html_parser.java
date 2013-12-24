package com.dimoshka.ua.classes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by designers on 24.12.13.
 */
public class class_html_parser {


    public Document get_html(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private String get_book_block(Document doc) {



        Elements books = doc.select("(<div lang=\"ru\" xml:lang=\"ru\" dir=\"ltr\" class=\"pubrow jsPubRow viewResult lang-ru dir-ltr publications pubSym.*>)(.*)</div>");
        for (Element book : books) {



        }

        return "";
    }


}
