package com.dimoshka.ua.classes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.dimoshka.ua.jwp.R;
import com.dimoshka.ua.jwp.main;

public class class_downloads_files extends Service {

	public static final int SERVICE_ID = 0x101104;
	public static final int BYTES_BUFFER_SIZE = 2 * 1024;
	public class_functions funct = new class_functions();
	private NotificationManager notificationManager;
	private final IBinder binder = new FileDownloadBinder();
	private AsyncDownloadTask task = null;
	protected static boolean isRunning = false;
	private Map<String, String> targetFile = null;
	private ArrayList<Map<String, String>> targetFiles = null;

	public class FileDownloadBinder extends Binder {
		class_downloads_files getService() {
			return class_downloads_files.this;
		}
	}

	public static boolean isRunning() {
		return isRunning;
	}

	@Override
	public void onCreate() {
		if (isRunning)
			return;
		else
			isRunning = true;
		targetFiles = new ArrayList<Map<String, String>>();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Log.i("JWP" + getClass().getName(), "START SERVICE");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("JWP" + getClass().getName(), "onStartCommand");
		String dir = funct.get_dir_app(getBaseContext()) + "/downloads/";
		File Directory = new File(dir);
		if (!Directory.isDirectory()) {
			Directory.mkdirs();
		}

		targetFile = new HashMap<String, String>();
		targetFile.put("link", intent.getStringExtra("file_url"));
		targetFile.put("putch", intent.getStringExtra("file_putch"));
		targetFiles.add(targetFile);

		task = new AsyncDownloadTask();
		task.execute();

		return START_STICKY;
	}

	protected ArrayList<Map<String, String>> fet_targetFiles() {
		return targetFiles;
	}

	@Override
	public void onDestroy() {
		notificationManager.cancelAll();

		if (task != null) {
			if (!task.isCancelled())
				task.cancel(true);
		}

		isRunning = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	protected Class<?> getIntentForLatestInfo() {
		return main.class;
	}

	protected int getNotificationFlag() {
		return Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS;
	}

	protected void onFinishDownload(int successCount,
			HashMap<String, String> failedFiles,
			HashMap<String, String> finishFiles) {

		for (Entry<String, String> entry : finishFiles.entrySet()) {

			Log.e("Finish downloading", entry.getValue());
		}

	}

	/**
	 * 
	 * @return
	 */
	protected int getNotificationIcon() {
		return R.drawable.ic_launcher;
	}

	protected RemoteViews getProgressView(int currentNumFile,
			int totalNumFiles, int currentReceivedBytes, int totalNumBytes,
			String filename) {
		RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.notification);
		contentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
		contentView.setTextViewText(R.id.text1, filename);
		contentView.setProgressBar(R.id.progress, 100, 100
				* currentReceivedBytes / totalNumBytes, false);
		contentView.setTextViewText(R.id.text2, String.format(
				"Progress (%d / %d)", currentNumFile, totalNumFiles));
		return contentView;
	}

	@SuppressWarnings("deprecation")
	protected void showNotification_popup(String ticker, String title,
			String content) {
		Notification notification = new Notification(getNotificationIcon(),
				ticker, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getIntentForLatestInfo()),
				Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notification.setLatestEventInfo(getApplicationContext(), title,
				content, contentIntent);
		notification.flags = getNotificationFlag();
		notificationManager.notify(SERVICE_ID, notification);
	}

	protected void showNotification(RemoteViews remoteView, String ticker) {
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(getNotificationIcon(),
				ticker, System.currentTimeMillis());
		notification.contentView = remoteView;
		notification.contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getIntentForLatestInfo()),
				Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notification.flags = getNotificationFlag();
		notificationManager.notify(SERVICE_ID, notification);
	}

	protected int getConnectTimeout() {
		return 10000;
	}

	protected int getReadTimeout() {
		return 10000;
	}

	private class AsyncDownloadTask extends AsyncTask<Void, Void, Void> {
		private ArrayList<Map<String, String>> targetFiles = null;
		private int total_file = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			targetFiles = fet_targetFiles();
			total_file = targetFiles.size();
		}

		public int getFileSizeAtURL(URL url) {
			int filesize = -1;
			try {
				HttpURLConnection http = (HttpURLConnection) url
						.openConnection();
				filesize = http.getContentLength();
				http.disconnect();
			} catch (Exception e) {
				// Logger.e(e.toString());
			}
			return filesize;
		}

		@SuppressLint("DefaultLocale")
		@Override
		protected Void doInBackground(Void... params) {
			String remoteFilepath, localFilepath;

			Log.i("JWP", "downloading: '" + total_file);

			for (int i = 0; i < targetFiles.size(); i++) {
				Map<String, String> targetFile = targetFiles.get(i);

				remoteFilepath = targetFile.get("link");
				localFilepath = targetFile.get("putch");
				Log.i("JWP" + getClass().getName(), "downloading: '"
						+ remoteFilepath + "' => '" + localFilepath + "'");

				try {

					File localFile = new File(localFilepath);
					if (!localFile.exists()) {

						if (isCancelled())
							return null;

						URL url = new URL(remoteFilepath);
						int filesize = getFileSizeAtURL(url);

						int loopCount = 0;
						if (filesize > 0) {
							URLConnection connection = url.openConnection();
							connection.setConnectTimeout(getConnectTimeout());
							connection.setReadTimeout(getReadTimeout());

							BufferedInputStream bis = new BufferedInputStream(
									connection.getInputStream());
							FileOutputStream fos = new FileOutputStream(
									new File(localFilepath));
							int bytesRead, totalBytesRead = 0;
							byte[] bytes = new byte[BYTES_BUFFER_SIZE];
							String progress, kbytes;
							while (!isCancelled()
									&& (bytesRead = bis.read(bytes)) != -1) {
								totalBytesRead += bytesRead;
								fos.write(bytes, 0, bytesRead);

								if (!isCancelled() && loopCount++ % 20 == 0) {
									RemoteViews progressView = getProgressView(
											i + 1, total_file, totalBytesRead,
											filesize,
											new File(localFilepath).getName());

									if (!isCancelled())
										showNotification(progressView,
												"Downloading File(s)");
								}
							}
							fos.close();
							bis.close();

							if (isCancelled())
								return null;
						} else {
							Log.i("JWP" + getClass().getName(),
									"file size unknown for remote file: "
											+ remoteFilepath);
						}
					}
				} catch (Exception e) {
					Log.e("JWP" + getClass().getName(), e.toString());

					showNotification_popup(getString(R.string.download_failed),
							getString(R.string.download_title), "Failed: "
									+ (new File(remoteFilepath)).getName());
				}
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.e("JWP" + getClass().getName(), "SERVICE Cancelled");
			showNotification_popup(getString(R.string.download_cancelled),
					getString(R.string.download_title),
					getString(R.string.download_cancelled));
		}

		@SuppressLint("DefaultLocale")
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			try {

				// onFinishDownload(successCount, failedFiles, finishFiles);

				showNotification_popup(getString(R.string.download_finished),
						getString(R.string.download_title),
						getString(R.string.download_finished));

			} catch (Exception e) {
				Log.e("JWP" + getClass().getName(), e.toString());
			}
		}
	}

	@SuppressLint("DefaultLocale")
	protected String getStringByteSize(int size) {
		if (size > 1024 * 1024) // mega
		{
			return String.format("%.1f MB", size / (float) (1024 * 1024));
		} else if (size > 1024) // kilo
		{
			return String.format("%.1f KB", size / 1024.0f);
		} else {
			return String.format("%d B");
		}
	}

}
