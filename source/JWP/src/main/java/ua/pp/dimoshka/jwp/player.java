package ua.pp.dimoshka.jwp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import java.io.File;

import ua.pp.dimoshka.adapter.player_adapter;
import ua.pp.dimoshka.classes.class_functions;
import ua.pp.dimoshka.classes.class_mediaplayer;
import ua.pp.dimoshka.classes.class_sqlite;
import ua.pp.dimoshka.classes.service_downloads_files;

public class player extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private ImageButton buttonPlayStop = null;
    private SeekBar seekBar = null;
    private final Handler handler = new Handler();
    private ListView listView = null;
    private Integer id_magazine = null;

    private class_mediaplayer mediaplayer_class = null;
    private SharedPreferences prefs = null;
    private SQLiteDatabase database = null;
    private class_functions funct = null;
    private SharedPreferences.OnSharedPreferenceChangeListener listener_pref = null;

    AFListener afListenerMusic = null;
    MediaPlayer mpMusic = null;

    private player_adapter mAdapter = null;

    private final Handler handler_completion = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            @SuppressWarnings("RedundantCast") int select = (int) listView.getCheckedItemPosition();
            if (select < listView.getCount() - 1) {
                listView.setItemChecked(select + 1, true);
                play(Integer.valueOf(select + 1));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name_shot);
        actionBar.setSubtitle(R.string.player);

        Bundle extras = getIntent().getExtras();
        id_magazine = Integer.valueOf(extras.getInt("id_magazine"));
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        funct = new class_functions(this);
        class_sqlite dbOpenHelper = new class_sqlite(this);
        database = dbOpenHelper.openDataBase();
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initViews();

        mAdapter = new player_adapter(this,
                new String[]{"title"}, new int[]{android.R.id.text1});
        listView.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(0, null, this);

    }

    private void initViews() {
        try {
            listView = (ListView) findViewById(R.id.list);
            buttonPlayStop = (ImageButton) findViewById(R.id.ButtonPlayStop);

            ImageButton buttonNext = (ImageButton) findViewById(R.id.ButtonNext);
            ImageButton buttonBack = (ImageButton) findViewById(R.id.ButtonBack);

            seekBar = (SeekBar) findViewById(R.id.SeekBar01);

            mediaplayer_class = new class_mediaplayer(buttonPlayStop, seekBar, handler_completion);

            buttonPlayStop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClick_Play();
                }
            });

            buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClick_Next();
                }
            });

            buttonBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClick_Back();
                }
            });

            buttonPlayStop.setEnabled(false);

            seekBar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    seekChange(v);
                    return false;
                }

            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    play(Integer.valueOf(position));
                }
            });

        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    private void play(Integer position) {
        buttonPlayStop.setEnabled(false);
        Cursor cursor = ((player_adapter) listView.getAdapter()).getCursor();
        cursor.moveToPosition(position.intValue());

        String name = cursor.getString(cursor
                .getColumnIndex("name"));
        String link = cursor.getString(cursor
                .getColumnIndex("link"));
        int file_enable = cursor.getInt(cursor
                .getColumnIndex("file"));
        File file = new File(funct.get_dir_app()
                + "/downloads/" + name);
        if (file.exists()) {
            if (file_enable == 0)
                funct.update_file_isn(database, name, Integer.valueOf(1));
            file_enable = 1;

        } else {
            if (file_enable == 1)
                funct.update_file_isn(database, name, Integer.valueOf(0));
            file_enable = 0;
        }

        if (file_enable == 1) {
            madia_player(file);
        } else {
            start_download(link, file);
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

    private void refresh() {
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @SuppressLint("HandlerLeak")
    private void madia_player(final File file) {
        if (mediaplayer_class.isPlaying()) {
            mediaplayer_class.stop();
        }
        mediaplayer_class.reset();
        mediaplayer_class.setDataSource(file.getAbsolutePath());
        mediaplayer_class.prepareAsync();

        startPlayProgressUpdater();
    }

    private void start_download(String link, File file) {
        Intent i = new Intent(getBaseContext(), service_downloads_files.class);
        i.putExtra("file_url", link);
        i.putExtra("file_putch", file.getAbsolutePath());
        Toast.makeText(this, getString(R.string.download_task_added),
                Toast.LENGTH_SHORT).show();
        startService(i);
    }

    void startPlayProgressUpdater() {
        try {
            if (mediaplayer_class.isPlaying()) {
                seekBar.setProgress(mediaplayer_class.getCurrentPosition());
            } else {
                buttonPlayStop.setImageResource(R.drawable.ic_av_play);
                seekBar.setProgress(0);
            }

            Runnable notification = new Runnable() {
                public void run() {
                    startPlayProgressUpdater();
                }
            };
            handler.postDelayed(notification, 1000);
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    private void seekChange(View v) {
        if (mediaplayer_class.isPlaying()) {
            SeekBar sb = (SeekBar) v;
            mediaplayer_class.seekTo(sb.getProgress());
        }
    }

    private void buttonClick_Play() {
        if (!mediaplayer_class.isPlaying()) {
            buttonPlayStop.setImageResource(R.drawable.ic_av_pause);
            mediaplayer_class.start();
            startPlayProgressUpdater();
        } else {
            buttonPlayStop.setImageResource(R.drawable.ic_av_play);
            mediaplayer_class.pause();
        }

    }

    private void buttonClick_Next() {
        @SuppressWarnings("RedundantCast") int select = (int) listView.getCheckedItemPosition();
        if (select < listView.getCount() - 1) {
            listView.setItemChecked(select + 1, true);
            play(Integer.valueOf(select + 1));
        }
    }

    private void buttonClick_Back() {
        @SuppressWarnings("RedundantCast") int select = (int) listView.getCheckedItemPosition();
        if (select > 0) {
            listView.setItemChecked(select - 1, true);
            play(Integer.valueOf(select - 1));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item0:
                System.exit(0);
                break;
            case R.id.item2:
                load_all_file();
                break;
            case R.id.item1:
                Intent i = new Intent(this, preferences.class);
                startActivity(i);
                break;
            case R.id.item3:
                refresh();
                break;
            default:
                break;
        }
        return false;
    }


    private void load_all_file() {
        Cursor cursor = ((player_adapter) listView.getAdapter()).getCursor();
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String name = cursor.getString(cursor
                    .getColumnIndex("name"));
            String link = cursor.getString(cursor
                    .getColumnIndex("link"));
            int file_enable = cursor.getInt(cursor
                    .getColumnIndex("file"));
            File file = new File(funct.get_dir_app()
                    + "/downloads/" + name);
            if (!file.exists()) {
                if (file_enable == 1)
                    funct.update_file_isn(database, name, Integer.valueOf(0));
                start_download(link, file);
            }
            cursor.moveToNext();
        }
    }


    protected void onDestroy() {
        super.onDestroy();
        mediaplayer_class.release();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MyCursorLoader(this, database, id_magazine.intValue());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }


    static class MyCursorLoader extends CursorLoader {
        SQLiteDatabase database;
        int id_magazine;

        public MyCursorLoader(Context context, SQLiteDatabase database, int id_magazine) {
            super(context);
            this.database = database;
            this.id_magazine = id_magazine;
        }

        @Override
        public Cursor loadInBackground() {
            @SuppressWarnings("UnnecessaryLocalVariable") Cursor cursor = database
                    .rawQuery(
                            "select files._id, id_type, file, type.name as name_type, files.name, link, files.title from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
                                    + id_magazine
                                    + "' and id_type='3' order by files.name asc",
                            null
                    );
            return cursor;
        }

    }

    class AFListener implements AudioManager.OnAudioFocusChangeListener {
        String label = "";
        MediaPlayer mp;

        public AFListener(MediaPlayer mp, String label) {
            this.label = label;
            this.mp = mp;
        }

        @SuppressWarnings("unused")
        @Override
        public void onAudioFocusChange(int focusChange) {
            String event = "";
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    event = "AUDIOFOCUS_LOSS";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    event = "AUDIOFOCUS_GAIN";
                    break;
            }
        }
    }

}
