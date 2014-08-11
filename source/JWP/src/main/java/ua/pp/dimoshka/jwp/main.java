package ua.pp.dimoshka.jwp;

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
import android.support.v4.content.LocalBroadcastManager;
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
import com.google.analytics.tracking.android.EasyTracker;

import java.util.List;
import java.util.Vector;

import ua.pp.dimoshka.classes.class_books_brochures;
import ua.pp.dimoshka.classes.class_functions;
import ua.pp.dimoshka.classes.class_open_or_download;
import ua.pp.dimoshka.classes.class_rss_jornals;
import ua.pp.dimoshka.classes.class_rss_news;
import ua.pp.dimoshka.classes.class_sqlite;
import ua.pp.dimoshka.classes.class_video;
import ua.pp.dimoshka.classes.service_downloads_files;

public class main extends ActionBarActivity implements ActionBar.TabListener {

    private int curent_tab = 0;
    private SQLiteDatabase database = null;
    private class_sqlite dbOpenHelper = null;
    private class_functions funct = null;

    private class_open_or_download open_or_download = null;
    private class_rss_jornals rss_jornals = null;
    private class_rss_news rss_news = null;
    private class_video video = null;
    private class_books_brochures books_brochures = null;

    private SharedPreferences prefs = null;
    private ViewPager pager = null;
    private ActionBar actionBar = null;
    private Boolean refresh_all = Boolean.FALSE;

    private List<Fragment> fragment_list = new Vector<Fragment>();
    private MyPagerAdapter pagerAdapter = null;
    private Boolean change_prefference = Boolean.FALSE;

    public SQLiteDatabase get_database() {
        return database;
    }

    public class_functions get_funct() {
        return funct;
    }

    public class_open_or_download get_open_or_download() {
        return open_or_download;
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler_jornals = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (refresh_all.booleanValue()) {
                        rss_news.get_all_feeds_activity(handler_news);
                    } else {
                        Log.d("JWP", "refrashe afte load");
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
                    if (refresh_all.booleanValue()) {
                        video.verify_all();
                    } else {
                        Log.d("JWP", "refrashe afte load");
                        refresh();
                    }
                    break;
            }
        }

    };

    @SuppressLint("HandlerLeak")
    private final Handler handler_video = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (refresh_all.booleanValue()) {
                        books_brochures.verify_all();
                    } else {
                        Log.d("JWP", "refrashe afte load");
                        refresh();
                    }
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
                    Log.d("JWP", "refrashe afte load");
                    refresh();
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            BugSenseHandler.initAndStartSession(this, "63148966");
            setContentView(R.layout.main);

            AQuery aq = new AQuery(this);
            funct = new class_functions(this);
            dbOpenHelper = new class_sqlite(this);
            database = dbOpenHelper.openDataBase();
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            open_or_download = new class_open_or_download(this, database, funct);
            pager = (ViewPager) findViewById(R.id.pager);

            actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setTitle(R.string.app_name_shot);
            actionBar.setSubtitle(R.string.app_name);

            FragmentManager frafment_mn = getSupportFragmentManager();
            pagerAdapter = new MyPagerAdapter(frafment_mn, fragment_list);
            pager.setAdapter(pagerAdapter);

            load_first();

            if (prefs.getBoolean("first_run", true)) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.first_run_title))
                        .setMessage(getString(R.string.first_run_text))
                        .setNeutralButton("OK", null).show();
                prefs.edit().putBoolean("first_run", false).apply();
            } else if (prefs.getBoolean("downloads_on_start", false)) {
                load_rss();
            }
            prefs.registerOnSharedPreferenceChangeListener(PreferenceChangeListener);
            pager.setOnPageChangeListener(PageChangeListener);


        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    private void load_first() {
        try {
            funct.get_language(get_database(), prefs);
            create_tabs();
            create_fragments();

            if (change_prefference.booleanValue()) refresh();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    private void create_fragments() {
        try {
            fragment_list.clear();
            rss_jornals = new class_rss_jornals(this, handler_jornals, database, funct);
            fragment_list.add(new jornals());
            rss_news = new class_rss_news(this, database, funct);
            fragment_list.add(new news());
            video = new class_video(this, handler_video, database, funct);
            fragment_list.add(new video());
            books_brochures = new class_books_brochures(this, handler_books_brochures, database, funct);
            fragment_list.add(new books_brochures());
            pagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    private void create_tabs() {
        try {
            if (actionBar.getTabCount() > 0) actionBar.removeAllTabs();
            ActionBar.Tab jornals_Tab = actionBar.newTab().setText(R.string.jornals)
                    .setTabListener(this);
            actionBar.addTab(jornals_Tab);
            ActionBar.Tab news_Tab = actionBar.newTab().setText(R.string.news)
                    .setTabListener(this);
            actionBar.addTab(news_Tab);
            ActionBar.Tab video_Tab = actionBar.newTab().setText(R.string.video)
                    .setTabListener(this);
            actionBar.addTab(video_Tab);
            ActionBar.Tab publication_Tab = actionBar.newTab().setText(R.string.books_brochures)
                    .setTabListener(this);
            actionBar.addTab(publication_Tab);
            curent_tab = 0;
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        curent_tab = tab.getPosition();
        pager.setCurrentItem(tab.getPosition());
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

    private OnSharedPreferenceChangeListener PreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            change_prefference = Boolean.TRUE;
            Log.d("PREFF_UPDATE", key);
            load_first();
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
                    refresh_all = Boolean.TRUE;
                    rss_jornals.get_all_feeds();
                } else {
                    switch (curent_tab) {
                        case 0:
                            rss_jornals.get_all_feeds();
                            break;
                        case 1:
                            rss_news.get_all_feeds_activity(handler_news);
                            break;
                        case 2:
                            video.verify_all();
                            break;
                        case 3:
                            books_brochures.verify_all();
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
            Intent intent = new Intent("update");
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
            broadcastManager.sendBroadcast(intent);
            Log.d("FRAGMENT", "start_refresh");
            refresh_all = Boolean.FALSE;
            pagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    @Override
    public void onDestroy() {
        stopService(
                new Intent(this, service_downloads_files.class));
        dbOpenHelper.close();
        database.close();
        super.onDestroy();
    }

}