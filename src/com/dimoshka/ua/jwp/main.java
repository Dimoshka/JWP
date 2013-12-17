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
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.dimoshka.ua.classes.class_functions;
import com.dimoshka.ua.classes.class_open_or_download;
import com.dimoshka.ua.classes.class_rss_books_brochures_img;
import com.dimoshka.ua.classes.class_rss_jornals;
import com.dimoshka.ua.classes.class_rss_news;
import com.dimoshka.ua.classes.class_sqlite;
import com.google.analytics.tracking.android.EasyTracker;

import java.util.List;
import java.util.Vector;

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
    private class_rss_news rss_news;
    private class_rss_books_brochures_img rss_books_brochures_img;

    jornals frag1;
    news frag2;
    books_brochures frag3;

    private ViewPager pager;
    private ActionBar actionBar;
    private ActionBar.Tab jornals_Tab;
    private ActionBar.Tab news_Tab;
    private ActionBar.Tab publication_Tab;
    List<Fragment> fragments;
    private MyPagerAdapter pagerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "63148966");
        BugSenseHandler.setLogging(100);
        setContentView(R.layout.main);

        frag1 = new jornals();
        frag2 = new news();
        frag3 = new books_brochures();

        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOnPageChangeListener(PageChangeListener);

        dbOpenHelper = new class_sqlite(this);
        database = dbOpenHelper.openDataBase();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        id_lang = Integer.parseInt(prefs.getString("language", "0"));

        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setTitle(R.string.app_name_shot);
        actionBar.setSubtitle(R.string.app_name);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.color.orange));

        jornals_Tab = actionBar.newTab().setText(R.string.jornals)
                .setTabListener(new MyTabListener());
        news_Tab = actionBar.newTab().setText(R.string.news)
                .setTabListener(new MyTabListener());
        publication_Tab = actionBar.newTab().setText(R.string.books_brochures)
                .setTabListener(new MyTabListener());

        actionBar.addTab(jornals_Tab);
        actionBar.addTab(news_Tab);
        actionBar.addTab(publication_Tab);


        rss_jornals = new class_rss_jornals(this, id_lang, handler_jornals,
                database);
        rss_news = new class_rss_news(this, id_lang, handler_news, database);
        rss_books_brochures_img = new class_rss_books_brochures_img(this,
                handler_books_brochures, database);

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
            rss_books_brochures_img.verify_all_img();
        }


        listener_pref = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs,
                                                  String key) {
                id_lang = Integer.parseInt(prefs.getString("language", "0"));
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener_pref);
        open_or_download = new class_open_or_download(this, database);
        refresh_pager();
    }

    private void refresh_pager() {
        List<Fragment> fragments = new Vector<Fragment>();
        fragments.add(frag1);
        fragments.add(frag2);
        fragments.add(frag3);
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager(),
                fragments);

        pager.setAdapter(pagerAdapter);
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
                    Log.e("JWP", "refrashe afte load");
                    refresh();
                    break;
            }
        }
    };

    private SimpleOnPageChangeListener PageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            actionBar.setSelectedNavigationItem(position);
        }
        ;
    };

    private class MyTabListener implements ActionBar.TabListener {
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
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
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // TODO Auto-generated method stub

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
                        rss_jornals.get_all_feeds();
                        break;
                    case 1:
                        rss_news.get_all_feeds();
                        break;
                    case 2:
                        frag3.refresh();
                        break;
                    default:
                        break;
                }
            } else
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT)
                        .show();
        } catch (Exception e) {
            funct.send_bug_report(this, e, "main", 148);
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
            funct.send_bug_report(this, e, "main", 157);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dbOpenHelper.close();
    }

}