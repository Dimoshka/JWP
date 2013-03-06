package com.dimoshka.ua.jwp;

import java.io.File;
import java.io.FileInputStream;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;

import com.dimoshka.ua.classes.class_activity_extends;
import com.dimoshka.ua.classes.class_simplecursoradapter_player;
import com.dimoshka.ua.classes.class_sqlite;

public class player extends class_activity_extends {

	private Button buttonPlayStop;
	private MediaPlayer mediaPlayer;
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
	}

	@SuppressWarnings("deprecation")
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

			stopManagingCursor(cursor);
			cursor = database
					.rawQuery(
							"select files._id, id_type, file, type.name as name_type, files.name, link, title from files left join magazine on files.id_magazine=magazine._id left join type on files.id_type=type._id where files.id_magazine='"
									+ id_magazine
									+ "' and id_type='3' order by files._id asc",
							null);

			startManagingCursor(cursor);
			scAdapter = new class_simplecursoradapter_player(this,
					R.layout.list_items_song, cursor, new String[] { "title" },
					new int[] { R.id.text1 });
			listView.setAdapter(scAdapter);

			listView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {

					buttonPlayStop.setEnabled(false);
					
					String name = cursor.getString(cursor
							.getColumnIndex("name"));
					int file_enable = cursor.getInt(cursor
							.getColumnIndex("file"));

					File file = new File(funct.get_dir_app(getBaseContext())
							+ "/downloads/" + name);
					if (file.exists() != true) {
						if (file_enable == 1)
							funct.update_file_isn(database, name, 0);
						file_enable = 0;
					} else {
						if (file_enable == 0)
							funct.update_file_isn(database, name, 1);
						file_enable = 1;
					}

					if (file_enable == 1) {
						Uri uri = Uri.parse(file.getAbsolutePath());
						try {
							mediaPlayer = new MediaPlayer();					
							FileInputStream fis = new FileInputStream(file.getAbsolutePath());
							mediaPlayer.setDataSource(fis.getFD());
							mediaPlayer.prepareAsync();
													
							mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
							    @Override
							    public void onPrepared(MediaPlayer mp) {
							    	buttonPlayStop.setEnabled(true);
							    	 seekBar.setMax(mediaPlayer.getDuration());
							    }
							});	
							
						} catch (Exception e) {
							funct.send_bug_report(getBaseContext(), e,
									getClass().getName(), 112);
						}
					}
				}
			});
		} catch (Exception e) {
			funct.send_bug_report(getBaseContext(), e, getClass().getName(),
					120);
		}
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

	protected void onDestroy() {
		super.onDestroy();
		stopManagingCursor(cursor);
		dbOpenHelper.close();
	}

}
