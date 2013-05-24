package com.dimoshka.ua.jwp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dimoshka.ua.classes.*;
import com.google.analytics.tracking.android.EasyTracker;

public class main extends SherlockFragmentActivity {

    private int curent_tab = 0;

    public static int id_lang = 0;
    public static SharedPreferences prefs;
    public static SQLiteDatabase database;
    public static class_sqlite dbOpenHelper;
    public static class_functions funct = new class_functions();
    public OnSharedPreferenceChangeListener listener_pref;
    public static class_open_or_download open_or_download;

    private class_rss_jornals rss_jornals;
    private class_rss_jornals_img rss_jornals_img;
    private class_rss_news rss_news;
    private class_rss_news_img rss_news_img;


    jornals frag1;
    news frag2;
    books_brochures frag3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        dbOpenHelper = new class_sqlite(this);
        database = dbOpenHelper.openDataBase();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        id_lang = Integer.parseInt(prefs.getString("language", "0"));

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(R.string.app_name_shot);

        ActionBar.Tab jornals_Tab = actionBar.newTab()
                .setText(R.string.jornals);
        ActionBar.Tab news_Tab = actionBar.newTab().setText(R.string.news);
        ActionBar.Tab publication_Tab = actionBar.newTab().setText(
                R.string.books_brochures);

        jornals_Tab.setTabListener(new MyTabListener());
        news_Tab.setTabListener(new MyTabListener());
        publication_Tab.setTabListener(new MyTabListener());

        frag1 = new jornals();
        frag2 = new news();
        frag3 = new books_brochures();

        actionBar.addTab(jornals_Tab);
        actionBar.addTab(news_Tab);
        actionBar.addTab(publication_Tab);


        rss_jornals = new class_rss_jornals(this, id_lang,
                handler_jornals, database);
        rss_jornals_img = new class_rss_jornals_img(this, handler_jornals,
                database);

        rss_news = new class_rss_news(this, id_lang, handler_news,
                database);
        rss_news_img = new class_rss_news_img(this, handler_news,
                database);

        rss_jornals.get_language(id_lang);
        rss_news.get_language(id_lang);


        boolean firstrun = prefs.getBoolean("first_run", true);
        if (firstrun) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.first_run_title))
                    .setMessage(getString(R.string.first_run))
                    .setNeutralButton("OK", null).show();
            prefs.edit().putBoolean("first_run", false).commit();
        } else if (prefs.getBoolean("downloads_on_start", false)) {
            // load_rss();
            rss_jornals.get_all_feeds();
            rss_news.get_all_feeds();
        }

        //load_rss();

        listener_pref = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs,
                                                  String key) {
                id_lang = Integer.parseInt(prefs.getString("language", "0"));
                // id_lang = rss_jornals.get_language(id_lang);
                // refresh();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener_pref);

        open_or_download = new class_open_or_download(this, database);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (prefs.getBoolean("analytics", true)) {
            EasyTracker.getInstance().activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (prefs.getBoolean("analytics", true)) {
            EasyTracker.getInstance().activityStop(this);
        }
    }


    @SuppressLint("HandlerLeak")
    private final Handler handler_jornals = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (prefs.getBoolean("downloads_img", true)) {
                        Log.e("JWP", "start load image");
                        rss_jornals_img.verify_all_img();
                    } else {
                        refresh();
                    }
                    break;
                case 2:
                    Log.e("JWP", "refrashe afte load");
                    refresh();
                    break;
            }
        }
    };


    @SuppressLint("HandlerLeak")
    private final Handler handler_news = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (prefs.getBoolean("downloads_img", true)) {
                        Log.e("JWP", "start load image");
                        rss_news_img.verify_all_img();
                    } else {
                        refresh();
                    }
                    break;
                case 2:
                    Log.e("JWP", "refrashe afte load");
                    refresh();
                    break;
            }
        }

    };


    private class MyTabListener implements ActionBar.TabListener {
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            curent_tab = tab.getPosition();

            switch (curent_tab) {
                case 0:
                    ft.replace(R.id.fragment_container, frag1);
                    break;
                case 1:
                    ft.replace(R.id.fragment_container, frag2);
                    break;
                case 2:
                    ft.replace(R.id.fragment_container, frag3);
                    break;
                default:
                    break;
            }
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            switch (curent_tab) {
                case 0:
                    ft.remove(frag1);
                    break;
                case 1:
                    ft.remove(frag2);
                    break;
                case 2:
                    ft.remove(frag3);
                    break;
                default:
                    break;
            }
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // TODO Auto-generated method stub
            refresh();
            // ACRA.getErrorReporter().handleSilentException(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.publication, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item0:
                System.exit(0);
                break;
            case R.id.item1:
                Intent i = new Intent(this, preferences.class);
                startActivity(i);
                break;
            case R.id.item2:
                load_rss();
                break;
            case R.id.item3:
                refresh();
                break;
            default:
                break;
        }

        return false;
    }

    private void load_rss() {
        try {
            if (funct.isNetworkAvailable(this) == true) {
                switch (curent_tab) {
                    case 0:
                        //frag1.load_rss();
                        rss_jornals.get_all_feeds();
                        break;
                    case 1:
                        //frag2.load_rss();
                        rss_news.get_all_feeds();
                        break;
                    case 2:
                        //frag3.load_rss();
                        frag3.refresh();
                        break;
                    default:
                        break;
                }
            } else
                Toast.makeText(this, R.string.no_internet,
                        Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            funct.send_bug_report(this, e, getClass().getName(), 148);
        }
    }

    private void refresh() {
        try {
            switch (curent_tab) {
                case 0:
                    frag1.refresh();
                    break;
                case 1:
                    frag2.refresh();
                    break;
                case 2:
                    frag3.refresh();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            funct.send_bug_report(this, e, getClass().getName(), 157);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        dbOpenHelper.close();
    }

}