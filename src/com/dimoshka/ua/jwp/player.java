package com.dimoshka.ua.jwp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_functions;
import com.dimoshka.ua.classes.class_mediaplayer;
import com.dimoshka.ua.classes.class_simplecursoradapter_player;
import com.dimoshka.ua.classes.class_sqlite;
import com.google.analytics.tracking.android.EasyTracker;
import java.io.File;

public class player extends SherlockActivity {

	private Button buttonPlayStop;
	private Button ButtonNext;
	private Button ButtonBack;
	// private MediaPlayer mediaPlayer = new MediaPlayer();
	private SeekBar seekBar;
	private final Handler handler = new Handler();
	private class_sqlite dbOpenHelper;
	private ListView listView;
	private Cursor cursor;
	private Integer id_magazine;
	private class_simplecursoradapter_player scAdapter;
	
    private class_mediaplayer mediaplayer_class;

	public SharedPreferences prefs;
	public SQLiteDatabase database;
	
    public class_functions funct = new class_functions();
	public int id_lang = 0;
	public OnSharedPreferenceChangeListener listener_pref;

    private ActionBar actionBar;

	AudioManager audioManager;
	AFListener afListenerMusic;
	MediaPlayer mpMusic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.jwp);
		super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background_textured_jwp));

		Bundle extras = getIntent().getExtras();
		id_magazine = extras.getInt("id_magazine");
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		dbOpenHelper = new class_sqlite(this);
		database = dbOpenHelper.openDataBase();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		initViews();
		refresh();
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

	private void initViews() {
		try {
			listView = (ListView) findViewById(R.id.list);
			buttonPlayStop = (Button) findViewById(R.id.ButtonPlayStop);

			ButtonNext = (Button) findViewById(R.id.ButtonNext);
			ButtonBack = (Button) findViewById(R.id.ButtonBack);

			seekBar = (SeekBar) findViewById(R.id.SeekBar01);

			mediaplayer_class = new class_mediaplayer(getApplicationContext(),
					buttonPlayStop, seekBar);

			buttonPlayStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					buttonClick_Play();
				}
			});

			ButtonNext.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					buttonClick_Next();
				}
			});

			ButtonBack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					buttonClick_Back();
				}
			});

			buttonPlayStop.setEnabled(false);

			seekBar.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					seekChange(v);
					return false;
				}

			});

			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					buttonPlayStop.setEnabled(false);
					cursor.moveToPosition(position);
					String name = cursor.getString(cursor
							.getColumnIndex("name"));
					String link = cursor.getString(cursor
							.getColumnIndex("link"));
					int file_enable = cursor.getInt(cursor
							.getColumnIndex("file"));
					File file = new File(funct.get_dir_app(getBaseContext())
							+ "/downloads/" + name);
					if (file.exists()) {
						if (file_enable == 0)
							funct.update_file_isn(database, name, 1);
						file_enable = 1;

					} else {
						if (file_enable == 1)
							funct.update_file_isn(database, name, 0);
						file_enable = 0;
					}

					if (file_enable == 1) {
						madia_player(file);
					} else {
						start_download(link, file);
					}
				}
			});

		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, "player",
					120);
		}
	}

	@SuppressWarnings("deprecation")
	private void refresh() {
		stopManagingCursor(cursor);
		cursor = database
				.rawQuery(
						"select files._id, id_type, file, type.name as name_type, files.name, link, files.title from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
								+ id_magazine
								+ "' and id_type='3' order by files.name asc",
						null);

		startManagingCursor(cursor);
		scAdapter = new class_simplecursoradapter_player(this,
				android.R.layout.simple_list_item_single_choice, cursor,
				new String[] { "title" }, new int[] { android.R.id.text1 });
		listView.setAdapter(scAdapter);
	}

	@SuppressLint("HandlerLeak")
	private void madia_player(final File file) {

		/*
		 * mpMusic = new MediaPlayer(); try {
		 * mpMusic.setDataSource(file.getAbsolutePath()); mpMusic.prepare(); }
		 * catch (IOException e) { e.printStackTrace(); }
		 * mpMusic.setOnCompletionListener(this);
		 * 
		 * afListenerMusic = new AFListener(mpMusic, "Music"); int requestResult
		 * = audioManager.requestAudioFocus(afListenerMusic,
		 * AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		 * mpMusic.start();
		 */

		if (mediaplayer_class.isPlaying()) {
			mediaplayer_class.stop();
		}
		mediaplayer_class.reset();
		mediaplayer_class.setDataSource(file.getAbsolutePath());
		mediaplayer_class.prepareAsync();

		startPlayProgressUpdater();
	}

	private void start_download(String link, File file) {
		Intent i = new Intent(getBaseContext(), class_downloads_files.class);
		i.putExtra("file_url", link);
		i.putExtra("file_putch", file.getAbsolutePath());
		Toast.makeText(this, getString(R.string.download_task_addeded),
				Toast.LENGTH_SHORT).show();
		startService(i);
	}

	public void startPlayProgressUpdater() {
		try {
			if (mediaplayer_class.isPlaying()) {
				seekBar.setProgress(mediaplayer_class.getCurrentPosition());
			} else {
				// mediaplayer_class.pause();
				buttonPlayStop.setText(getString(R.string.player_play));
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
		if (buttonPlayStop.getText() == getString(R.string.player_play)) {
			buttonPlayStop.setText(getString(R.string.player_pause));
			mediaplayer_class.start();
			startPlayProgressUpdater();
		} else {
			buttonPlayStop.setText(getString(R.string.player_play));
			mediaplayer_class.pause();
		}

	}

	private void buttonClick_Next() {
		int lelect = (int) listView.getSelectedItemId();
		if (lelect < listView.getCount() - 1) {
			listView.setSelection(lelect + 1);
		}
	}

	private void buttonClick_Back() {
		int lelect = (int) listView.getSelectedItemId();
		if (lelect > 0) {
			listView.setSelection(lelect - 1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.books_broshures, menu);
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
		case R.id.item3:
			refresh();
			break;
		default:
			break;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	protected void onDestroy() {
		super.onDestroy();
		mediaplayer_class.release();
		stopManagingCursor(cursor);
		dbOpenHelper.close();
	}

	@SuppressLint("NewApi")
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
