package com.dimoshka.ua.jwp;

import java.io.File;
import java.io.FileInputStream;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import com.dimoshka.ua.classes.class_activity_extends;
import com.dimoshka.ua.classes.class_downloads_files;
import com.dimoshka.ua.classes.class_simplecursoradapter_player;
import com.dimoshka.ua.classes.class_sqlite;

public class player extends class_activity_extends {

	private Button buttonPlayStop;
	private MediaPlayer mediaPlayer = null;
	private SeekBar seekBar;
	private final Handler handler = new Handler();
	private class_sqlite dbOpenHelper;
	private ListView listView;
	private Cursor cursor;
	private Integer id_magazine;
	private class_simplecursoradapter_player scAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		Bundle extras = getIntent().getExtras();
		id_magazine = extras.getInt("id_magazine");
		dbOpenHelper = new class_sqlite(this);
		database = dbOpenHelper.openDataBase();
		initViews();
		refresh();
	}

	private void initViews() {
		try {
			listView = (ListView) findViewById(R.id.list);
			buttonPlayStop = (Button) findViewById(R.id.ButtonPlayStop);
			buttonPlayStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					buttonClick();
				}
			});

			buttonPlayStop.setEnabled(false);

			seekBar = (SeekBar) findViewById(R.id.SeekBar01);
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
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					120);
		}
	}

	@SuppressWarnings("deprecation")
	private void refresh() {
		stopManagingCursor(cursor);
		cursor = database
				.rawQuery(
						"select files._id, id_type, file, type.name as name_type, files.name, link, title from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
								+ id_magazine
								+ "' and id_type='3' order by files.name asc",
						null);

		startManagingCursor(cursor);
		scAdapter = new class_simplecursoradapter_player(this,
				R.layout.list_items_song, cursor, new String[] { "title" },
				new int[] { R.id.text1 });
		listView.setAdapter(scAdapter);
	}

	@SuppressLint("HandlerLeak")
	private void madia_player(final File file) {
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}

		mediaPlayer = new MediaPlayer();

		try {
			final MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mediaPlayer) {
					buttonPlayStop.setEnabled(true);
					seekBar.setMax(mediaPlayer.getDuration());
				}
			};
			final MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.e(getPackageName(),
							String.format("Error(%s%s)", what, extra));
					return false;
				}
			};

			FileInputStream subsetInputStream = null;
			try {
				subsetInputStream = new FileInputStream(file.getAbsolutePath());
				mediaPlayer.setDataSource(subsetInputStream.getFD());
			} catch (Exception e) {
				funct.send_bug_report(getBaseContext(), e,
						getClass().getName(), 197);
			}
			mediaPlayer.setOnErrorListener(onErrorListener);
			// mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			onPreparedListener.onPrepared(mediaPlayer);

		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					201);
		}

	}

	private void start_download(String link, File file) {
		Intent i = new Intent(getBaseContext(), class_downloads_files.class);
		i.putExtra("file_url", link);
		i.putExtra("file_putch", file.getAbsolutePath());
		Toast.makeText(this, getString(R.string.download_task_addeded),
				Toast.LENGTH_SHORT).show();
		startService(i);
	}

	public void onPrepared(MediaPlayer player) {
		mediaPlayer.start();
	}

	public void startPlayProgressUpdater() {
		seekBar.setProgress(mediaPlayer.getCurrentPosition());

		if (mediaPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
				public void run() {
					startPlayProgressUpdater();
				}
			};
			handler.postDelayed(notification, 1000);
		} else {
			mediaPlayer.pause();
			buttonPlayStop.setText(getString(R.string.player_play));
			seekBar.setProgress(0);
		}
	}

	private void seekChange(View v) {
		if (mediaPlayer.isPlaying()) {
			SeekBar sb = (SeekBar) v;
			mediaPlayer.seekTo(sb.getProgress());
		}
	}

	private void buttonClick() {

		if (buttonPlayStop.getText() == getString(R.string.player_play)) {
			buttonPlayStop.setText(getString(R.string.player_pause));
			try {
				mediaPlayer.start();
				startPlayProgressUpdater();
			} catch (IllegalStateException e) {
				mediaPlayer.pause();
			}
		} else {
			buttonPlayStop.setText(getString(R.string.player_play));
			mediaPlayer.pause();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 3, Menu.NONE, R.string.refrashe).setIcon(
				android.R.drawable.ic_menu_rotate);
		menu.add(Menu.NONE, 1, Menu.NONE, R.string.preference).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(Menu.NONE, 0, Menu.NONE, R.string.exit).setIcon(
				android.R.drawable.ic_lock_power_off);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			System.exit(0);
			break;
		case 1:
			Intent i = new Intent(this, preferences.class);
			startActivity(i);
			break;
		case 3:
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
		if (mediaPlayer != null) {
			mediaPlayer.release();
		}
		stopManagingCursor(cursor);
		dbOpenHelper.close();
	}

}
