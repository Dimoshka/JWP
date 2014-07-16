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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.bugsense.trace.BugSenseHandler;
import com.dimoshka.ua.classes.class_books_brochures;
import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_functions;
import com.dimoshka.ua.classes.class_open_or_download;
import com.dimoshka.ua.classes.class_rss_jornals;
import com.dimoshka.ua.classes.class_rss_news;
import com.dimoshka.ua.classes.class_sqlite;
import com.google.analytics.tracking.android.EasyTracker;

import java.util.List;
import java.util.Vector;

public class main extends ActionBarActivity implements ActionBar.TabListener {

    private int curent_tab = 0;
    public static int id_lang = 0;

    public static SQLiteDatabase database;
    public static class_sqlite dbOpenHelper;
    public static class_functions funct;
    public static AQuery aq;

    public static class_open_or_download open_or_download;
    public static class_rss_jornals rss_jornals;
    public static class_rss_news rss_news;
    public static class_books_brochures books_brochures;

    private SharedPreferences prefs;
    private jornals jornals_fragm;
    private news news_fragm;
    private books_brochures book_fragm;
    private OnSharedPreferenceChangeListener listener_pref;
    private ViewPager pager;
    private ActionBar actionBar;
    private ActionBar.Tab jornals_Tab;
    private ActionBar.Tab news_Tab;
    private ActionBar.Tab publication_Tab;
    private List<Fragment> fragments;
    private MyPagerAdapter pagerAdapter;
    private Boolean refresh_all = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "63148966");
        setContentView(R.layout.main);

        aq = new AQuery(this);
        funct = new class_functions(this);
        dbOpenHelper = new class_sqlite(this, funct);
        database = dbOpenHelper.openDataBase();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        jornals_fragm = new jornals();
        news_fragm = new news();
        book_fragm = new books_brochures();

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOnPageChangeListener(PageChangeListener);
        id_lang = Integer.parseInt(prefs.getString("language", "0"));

        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(R.string.app_name_shot);
        actionBar.setSubtitle(R.string.app_name);
        jornals_Tab = actionBar.newTab().setText(R.string.jornals)
                .setTabListener(this);
        news_Tab = actionBar.newTab().setText(R.string.news)
                .setTabListener(this);
        publication_Tab = actionBar.newTab().setText(R.string.books_brochures)
                .setTabListener(this);

        actionBar.addTab(jornals_Tab);
        actionBar.addTab(news_Tab);
        actionBar.addTab(publication_Tab);


        rss_jornals = new class_rss_jornals(this, id_lang, handler_jornals,
                database, funct);
        rss_news = new class_rss_news(this, id_lang, handler_news, database, funct);
        books_brochures = new class_books_brochures(this,
                handler_books_brochures, database, funct);

        rss_jornals.get_language(id_lang);
        rss_news.get_language(id_lang);


        listener_pref = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs,
                                                  String key) {
                id_lang = Integer.parseInt(prefs.getString("language", "0"));
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener_pref);
        open_or_download = new class_open_or_download(this, database, funct);

        if (prefs.getBoolean("first_run", true)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.first_run_title))
                    .setMessage(getString(R.string.first_run))
                    .setNeutralButton("OK", null).show();
            prefs.edit().putBoolean("first_run", false).commit();
            refresh_pager();
        } else if (prefs.getBoolean("downloads_on_start", false)) {
            refresh_pager();
            load_rss();
        } else refresh_pager();
    }

    private void refresh_pager() {
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(jornals_fragm);
        fragments.add(news_fragm);
        fragments.add(book_fragm);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),
                fragments);
        pager.setAdapter(pagerAdapter);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        curent_tab = tab.getPosition();

        switch (curent_tab) {
            case 0:
                pager.setCurrentItem(0);
                break;
            case 1:
                pager.setCurrentItem(1);
                break;
            case 2:
                pager.setCurrentItem(2);
                break;
            default:
                break;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments;

        public MyPagerAdapter(FragmentManager fragmentManager,
                              List<Fragment> fragments) {
            super(fragmentManager);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (prefs.getBoolean("analytics", true)) {
            EasyTracker.getInstance(this).activityStart(this);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (prefs.getBoolean("analytics", true)) {
            EasyTracker.getInstance(this).activityStop(this);
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler_jornals = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (refresh_all) {
                        rss_news.get_all_feeds();
                    } else {
                        Log.e("JWP", "refrashe afte load");
                        refresh();
                    }
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
                    Log.e("JWP", "refrashe afte load");
                    refresh();
                    break;
            }
        }

    };

    @SuppressLint("HandlerLeak")
    private final Handler handler_books_brochures = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (refresh_all) {
                        rss_jornals.get_all_feeds();
                    } else {
                        Log.e("JWP", "refrashe afte load");
                        refresh();
                    }
            }
        }
    };

    private SimpleOnPageChangeListener PageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            actionBar.setSelectedNavigationItem(position);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
            if (funct.isNetworkAvailable()) {
                if (prefs.getBoolean("update_all_at_once", true)) {
                    refresh_all = true;
                    books_brochures.verify_all_img();
                } else {
                    switch (curent_tab) {
                        case 0:
                            rss_jornals.get_all_feeds();
                            break;
                        case 1:
                            rss_news.get_all_feeds();
                            break;
                        case 2:
                            books_brochures.verify_all_img();
                            break;
                        default:
                            break;
                    }
                }
            } else
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT)
                        .show();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    private void refresh() {
        try {
            if (refresh_all) {
                jornals_fragm.refresh();
                news_fragm.refresh();
                book_fragm.refresh();
                refresh_all = false;
            } else {
                switch (curent_tab) {
                    case 0:
                        jornals_fragm.refresh();
                        break;
                    case 1:
                        news_fragm.refresh();
                        break;
                    case 2:
                        book_fragm.refresh();
                        break;
                    default:
                        break;
                }
            }

        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    @Override
    public void onDestroy() {
        stopService(
                new Intent(this, class_downloads_files.class));
        dbOpenHelper.close();
        database.close();
        super.onDestroy();
    }

}