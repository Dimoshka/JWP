package com.dimoshka.ua.classes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
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

	// public static final int SERVICE_ID = 0x101104;
	public static final int BYTES_BUFFER_SIZE = 2 * 1024;
	public class_functions funct = new class_functions();
	private NotificationManager notificationManager;
	private final IBinder binder = new FileDownloadBinder();
	private AsyncDownloadTask task = null;
	protected static boolean isRunning = false;
	private HashMap<String, String> targetFiles = null;
	public static int NEXT_NOTIFICATION_ID = 0;

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

		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		targetFiles = new HashMap<String, String>();
		Log.d("JWP" + getClass().getName(), "START SERVICE");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("JWP" + getClass().getName(), "onStartCommand");
		String dir = funct.get_dir_app(getBaseContext()) + "/downloads/";
		File Directory = new File(dir);
		if (!Directory.isDirectory()) {
			Directory.mkdirs();
		}

		targetFiles.put(intent.getStringExtra("file_url"),
				intent.getStringExtra("file_putch"));
		task = new AsyncDownloadTask();
		task.execute();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (task != null) {
			if (!task.isCancelled())
				task.cancel(true);
		}

		isRunning = false;
	}

	protected HashMap<String, String> getTargetFiles() {
		return targetFiles;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	protected Class<?> getIntentForLatestInfo() {
		 return main.class;
		//return null;
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

		// contentView
		// .setImageViewBitmap(R.id.image, BitmapFactory.decodeFile(""));

		contentView.setTextViewText(R.id.text1, String.format(
				"Progress (%d / %d)", currentNumFile, totalNumFiles));

		contentView.setTextViewText(R.id.text2, filename);

		contentView.setProgressBar(R.id.progress, 100, 100
				* currentReceivedBytes / totalNumBytes, false);
		return contentView;
	}

	/**
	 * 
	 * @param title
	 * @param content
	 */
	@SuppressWarnings("deprecation")
	protected void showNotification(int id, String ticker, String title,
			String content) {
		Notification notification = new Notification(getNotificationIcon(),
				ticker, System.currentTimeMillis());
		//PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
		//		new Intent(this, getIntentForLatestInfo()),
		//		Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//notification.setLatestEventInfo(getApplicationContext(), title,
		//		content, contentIntent);
		notification.flags = getNotificationFlag();

		notificationManager.notify(id, notification);
	}

	/**
	 * 
	 * @param remoteView
	 * @param ticker
	 */

	protected void showNotification(int id, RemoteViews remoteView,
			String ticker) {
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(getNotificationIcon(),
				ticker, System.currentTimeMillis());
		notification.contentView = remoteView;
		notification.contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, getIntentForLatestInfo()),
				Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notification.flags = getNotificationFlag();

		notificationManager.notify(id, notification);
	}

	/**
	 * override this function to alter socket connect timeout value
	 * 
	 * @return
	 */
	protected int getConnectTimeout() {
		return 10000;
	}

	/**
	 * override this function to alter socket read timeout value
	 * 
	 * @return
	 */
	protected int getReadTimeout() {
		return 10000;
	}

	private class AsyncDownloadTask extends AsyncTask<Void, Void, Void> {
		private int successCount;
		private int numTotalFiles;
		private HashMap<String, String> targetFiles = null;
		private HashMap<String, String> failedFiles = null;
		private HashMap<String, String> finishFiles = null;
		private int notificationId = 1;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			successCount = 0;
			targetFiles = getTargetFiles();
			numTotalFiles = targetFiles.size();
			failedFiles = new HashMap<String, String>();
			finishFiles = new HashMap<String, String>();

			NEXT_NOTIFICATION_ID = 1 + NEXT_NOTIFICATION_ID;
			this.notificationId = NEXT_NOTIFICATION_ID;

			Log.d("JWP" + getClass().getName(), "AsyncDownloadTask");

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

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@SuppressLint("DefaultLocale")
		@Override
		protected Void doInBackground(Void... params) {
			String remoteFilepath, localFilepath;
			for (Entry<String, String> entry : targetFiles.entrySet()) {
				remoteFilepath = entry.getKey();
				localFilepath = entry.getValue();

				Log.d("JWP" + getClass().getName(), "downloading: '"
						+ remoteFilepath + "' => '" + localFilepath + "'");

				try {
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
						FileOutputStream fos = new FileOutputStream(new File(
								localFilepath));
						int bytesRead, totalBytesRead = 0;
						byte[] bytes = new byte[BYTES_BUFFER_SIZE];
						String progress, kbytes;
						while (!isCancelled()
								&& (bytesRead = bis.read(bytes)) != -1) {
							totalBytesRead += bytesRead;
							fos.write(bytes, 0, bytesRead);

							// don't show notification too often
							if (!isCancelled() && loopCount++ % 20 == 0) {
								RemoteViews progressView = getProgressView(
										successCount + 1, numTotalFiles,
										totalBytesRead, filesize, new File(
												localFilepath).getName());
								if (progressView == null) {
									progress = String.format(
											"Download Progress (%d / %d)",
											successCount + 1, numTotalFiles);
									kbytes = String.format("%s / %s",
											getStringByteSize(totalBytesRead),
											getStringByteSize(filesize));

									if (!isCancelled())
										showNotification(notificationId,
												"Downloading File(s)",
												progress, kbytes);
								} else {
									if (!isCancelled())
										showNotification(notificationId,
												progressView,
												"Downloading File(s)");
								}
							}
						}
						fos.close();
						bis.close();

						if (isCancelled())
							return null;

						successCount++;
						finishFiles.put(remoteFilepath, localFilepath);

					} else {
						Log.i("JWP" + getClass().getName(),
								"file size unknown for remote file: "
										+ remoteFilepath);
						failedFiles.put(remoteFilepath, localFilepath);
					}
				} catch (Exception e) {
					Log.e("JWP" + getClass().getName(), e.toString());

					showNotification(notificationId, "Download Failed",
							"Download Progress", "Failed: "
									+ (new File(remoteFilepath)).getName());

					failedFiles.put(remoteFilepath, localFilepath);
				}
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			Log.e("JWP" + getClass().getName(), "SERVICE Cancelled");
			showNotification(notificationId, "Download Cancelled",
					"Download Progress", "Cancelled");
		}

		@SuppressLint("DefaultLocale")
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			onFinishDownload(successCount, failedFiles, finishFiles);

			String finished;
			if (successCount != numTotalFiles)
				finished = String.format("Finished (%d download(s) failed)",
						numTotalFiles - successCount);
			else
				finished = "Finished";
			showNotification(notificationId, "Download Finished",
					"Download Progress", finished);

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
