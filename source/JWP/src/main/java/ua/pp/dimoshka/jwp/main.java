package ua.pp.dimoshka.jwp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.splunk.mint.Mint;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import ua.pp.dimoshka.classes.class_books_brochures;
import ua.pp.dimoshka.classes.class_functions;
import ua.pp.dimoshka.classes.class_kingdom_ministry;
import ua.pp.dimoshka.classes.class_open_or_download;
import ua.pp.dimoshka.classes.class_rss_journals;
import ua.pp.dimoshka.classes.class_rss_news;
import ua.pp.dimoshka.classes.class_sqlite;
import ua.pp.dimoshka.classes.class_video;
import ua.pp.dimoshka.fragment.journals;
import ua.pp.dimoshka.fragment.news;

public class main extends ActionBarActivity implements ActionBar.TabListener {

    private int curent_tab = 0;
    private SQLiteDatabase database = null;
    private class_sqlite dbOpenHelper = null;
    private class_functions funct = null;

    private class_open_or_download open_or_download = null;
    private class_rss_journals rss_journals = null;
    private class_rss_news rss_news = null;
    private class_video video = null;
    private class_kingdom_ministry kingdom_ministry = null;
    private class_books_brochures books_brochures = null;

    private SharedPreferences prefs = null;
    private ViewPager pager = null;
    private ActionBar actionBar = null;
    private Boolean refresh_all = Boolean.FALSE;

    private List<Fragment> fragment_list = new Vector<Fragment>();
    private MyPagerAdapter pagerAdapter = null;
    private Boolean change_prefference = Boolean.FALSE;
    private OnSharedPreferenceChangeListener PreferenceChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            change_prefference = Boolean.TRUE;
            Log.d("PREFF_UPDATE", key);
            load_first();
            pagerAdapter.notifyDataSetChanged();
        }
    };
    private Boolean progressbar = false;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals("loading")) {
                int status = intent.getIntExtra("status", 0);
                switch (status) {
                    case 1:
                        int page = intent.getIntExtra("page", 0);
                        switch (page) {
                            case 1:
                                if (refresh_all.booleanValue()) {
                                    rss_journals.get_all_feeds();
                                } else {
                                    Log.d("JWP", "refrashe afte load");
                                    refresh();
                                }
                                break;
                            case 2:
                                if ((refresh_all.booleanValue() && prefs.getBoolean("site_html", true) && !prefs.getBoolean("site_html_singly", true)) || prefs.getBoolean("first_load", true)) {
                                    video.verify_all();
                                } else {
                                    Log.d("JWP", "refrashe afte load");
                                    refresh();
                                }
                                break;
                            case 3:
                                if (refresh_all.booleanValue()) {
                                    kingdom_ministry.verify_all();
                                } else {
                                    Log.d("JWP", "refrashe afte load");
                                    refresh();
                                }
                                break;
                            case 4:
                                if (refresh_all.booleanValue()) {
                                    books_brochures.verify_all();
                                } else {
                                    Log.d("JWP", "refrashe afte load");
                                    refresh();
                                }
                                break;
                            case 5:
                                Log.d("JWP", "refrashe afte load");
                                refresh();
                                break;
                        }
                        progressbar = false;
                        break;
                    case 2:
                        progressbar = true;
                        break;
                    case 3:
                        progressbar = false;
                        refresh();
                        break;
                    default:
                        progressbar = false;
                        break;
                }
                setSupportProgressBarIndeterminateVisibility(progressbar);
            }
        }
    };
    private SimpleOnPageChangeListener PageChangeListener = new SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            actionBar.setSelectedNavigationItem(position);
        }
    };

    public SQLiteDatabase get_database() {
        return database;
    }

    public class_functions get_funct() {
        return funct;
    }

    public class_open_or_download get_open_or_download() {
        return open_or_download;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            funct = new class_functions(this);
            prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean("analytics", true)) {
                Mint.initAndStartSession(main.this, "63148966");
                Tracker t = ((AnalyticsSampleApp) this.getApplication()).getTracker(AnalyticsSampleApp.TrackerName.APP_TRACKER);
                t.setScreenName("main");
                t.send(new HitBuilders.AppViewBuilder().build());
            }
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
            setContentView(R.layout.main);


            dbOpenHelper = new class_sqlite(this);
            database = dbOpenHelper.openDataBase();

            open_or_download = new class_open_or_download(this, database, funct);
            pager = (ViewPager) findViewById(R.id.pager);

            actionBar = getSupportActionBar();
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setTitle(R.string.app_name_shot);
            actionBar.setSubtitle(R.string.app_name);

            load_first();
            FragmentManager frafment_mn = getSupportFragmentManager();
            pagerAdapter = new MyPagerAdapter(frafment_mn, fragment_list);
            pager.setAdapter(pagerAdapter);

            if (prefs.getBoolean("first_run", true)) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.first_run_title))
                        .setMessage(getString(R.string.first_run_text))
                        .setNeutralButton("OK", null).show();
                prefs.edit().putBoolean("first_run", false).apply();
                //funct.delete_dir_app();
            } else if (prefs.getBoolean("downloads_on_start", false)) {
                load_rss();
            }

            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
            IntentFilter intentFilter = new IntentFilter("loading");
            broadcastManager.registerReceiver(receiver, intentFilter);

            prefs.registerOnSharedPreferenceChangeListener(PreferenceChangeListener);
            pager.setOnPageChangeListener(PageChangeListener);
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        progressbar = savedInstanceState.getBoolean("progressbar");
        setSupportProgressBarIndeterminateVisibility(progressbar);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("progressbar", progressbar);
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
            rss_news = new class_rss_news(this, database, funct);
            fragment_list.add(new news());
            rss_journals = new class_rss_journals(this, database, funct);
            fragment_list.add(new journals());
            if (prefs.getBoolean("site_html", true)) {
                video = new class_video(this, database, funct);
                fragment_list.add(new ua.pp.dimoshka.fragment.video());
                kingdom_ministry = new class_kingdom_ministry(this, database, funct);
                fragment_list.add(new ua.pp.dimoshka.fragment.kingdom_ministry());
                books_brochures = new class_books_brochures(this, database, funct);
                fragment_list.add(new ua.pp.dimoshka.fragment.books_brochures());
            }
            //pagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    private void create_tabs() {
        try {
            if (actionBar.getTabCount() > 0) actionBar.removeAllTabs();
            ActionBar.Tab news_Tab = actionBar.newTab().setText(R.string.news)
                    .setTabListener(this);
            actionBar.addTab(news_Tab);
            ActionBar.Tab journals_Tab = actionBar.newTab().setText(R.string.journals)
                    .setTabListener(this);
            actionBar.addTab(journals_Tab);
            if (prefs.getBoolean("site_html", true)) {
                ActionBar.Tab video_Tab = actionBar.newTab().setText(R.string.video)
                        .setTabListener(this);
                actionBar.addTab(video_Tab);
                ActionBar.Tab kingdom_ministry_Tab = actionBar.newTab().setText(R.string.kingdom_ministry)
                        .setTabListener(this);
                actionBar.addTab(kingdom_ministry_Tab);
                ActionBar.Tab publication_Tab = actionBar.newTab().setText(R.string.books_brochures)
                        .setTabListener(this);
                actionBar.addTab(publication_Tab);
            }
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
                    if (curent_tab > 1 && prefs.getBoolean("site_html", true) && prefs.getBoolean("site_html_singly", true) && !prefs.getBoolean("first_load", true)) {
                        video.verify_all();
                    } else {
                        rss_news.get_all_feeds_activity();
                    }
                } else {
                    switch (curent_tab) {
                        case 0:
                            rss_news.get_all_feeds_activity();
                            break;
                        case 1:
                            rss_journals.get_all_feeds();
                            break;
                        case 2:
                            video.verify_all();
                            break;
                        case 3:
                            kingdom_ministry.verify_all();
                            break;
                        case 4:
                            books_brochures.verify_all();
                            break;
                        default:
                            break;
                    }
                }
            } else
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT)
                        .show();
        } catch (
                Exception e
                )

        {
            funct.send_bug_report(e);
        }

    }

    private void refresh() {
        try {
            if (prefs.getBoolean("first_load", true)) {
                funct.load_file_isn(database);
                prefs.edit().putBoolean("first_load", false).apply();
            }
            funct.send_to_local_brodcast("update", new HashMap<String, Integer>());
            Log.d("FRAGMENT", "start_refresh");
            refresh_all = Boolean.FALSE;
            pagerAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    @Override
    public void onDestroy() {
        //stopService(
        //        new Intent(this, service_downloads_files.class));
        //dbOpenHelper.close();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        //database.close();
        super.onDestroy();
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

}