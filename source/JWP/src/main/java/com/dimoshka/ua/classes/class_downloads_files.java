package com.dimoshka.ua.classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.dimoshka.ua.jwp.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class class_downloads_files extends Service {

    public static final int SERVICE_ID = 0x101104;
    public static final int BYTES_BUFFER_SIZE = 2 * 1024;
    public class_functions funct = new class_functions(getBaseContext());
    private NotificationManager notificationManager;
    private final IBinder binder = new FileDownloadBinder();
    private AsyncDownloadTask task = null;
    protected static boolean isRunning = false;
    private Map<String, String> targetFile = null;
    private ArrayList<Map<String, String>> targetFiles = null;
    private int now_targetFile = 0;

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
        try {
            Log.d("JWP" + getClass().getName(), "onStartCommand");
            String dir = funct.get_dir_app() + "/downloads/";
            File Directory = new File(dir);
            if (!Directory.isDirectory()) {
                Directory.mkdirs();
            }
            Directory = new File(dir + "temp/");
            if (!Directory.isDirectory()) {
                Directory.mkdirs();
            }
            targetFile = new HashMap<String, String>();
            targetFile.put("link", intent.getStringExtra("file_url"));
            targetFile.put("putch", intent.getStringExtra("file_putch"));
            targetFiles.add(targetFile);

            task = new AsyncDownloadTask();
            task.execute();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
        return START_STICKY;
    }

    protected ArrayList<Map<String, String>> get_targetFiles() {
        return targetFiles;
    }

    protected int get_targetFiles_num() {
        return targetFiles.size();
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll();
        Log.e("JWP" + getClass().getName(), "Service Cancelled");
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

    protected int getNotificationFlag() {
        return Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP;
    }

    protected void onFinishDownload(int success) {
        try {
            if (success > 0) {
                Map<String, String> targetFile = targetFiles.get(success - 1);
                File localFile = new File(targetFile.get("putch"));
                if (localFile.exists()) {

                    Log.d("JWP" + getClass().getName(), "Update to 1 - "
                            + localFile.getName());

                    class_sqlite dbOpenHelper = new class_sqlite(this, funct);
                    SQLiteDatabase database = dbOpenHelper.openDataBase();
                    funct.update_file_isn(database, localFile.getName(), 1);
                    dbOpenHelper.close();
                }
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

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
        contentView.setTextViewText(R.id.text2,
                String.format("(%d / %d)", currentNumFile, totalNumFiles));
        return contentView;
    }


    protected void showNotification_popup(String ticker, String title,
                                          String content, Context context) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(), Intent.FLAG_ACTIVITY_CLEAR_TOP);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title).setContentText(ticker)
                .setSmallIcon(getNotificationIcon()).setLargeIcon(null)
                .setContentIntent(contentIntent).setAutoCancel(true);
        Notification
                notification = builder.build();
        notificationManager.notify(SERVICE_ID, notification);

    }


    protected void showNotification(RemoteViews remoteView, String ticker,
                                    Context context) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new
                Intent(), Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(ticker).setContentText(ticker)
                .setSmallIcon(getNotificationIcon()).setLargeIcon(null)
                .setContentIntent(contentIntent).setAutoCancel(true)
                .setContent(remoteView);

        Notification notification = builder.build();
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
        private int success = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            targetFiles = get_targetFiles();
            total_file = get_targetFiles_num();
        }

        public int getFileSizeAtURL(URL url) {
            int filesize = -1;
            try {
                HttpURLConnection http = (HttpURLConnection) url
                        .openConnection();
                filesize = http.getContentLength();
                http.disconnect();
            } catch (Exception e) {

            }
            return filesize;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                String remoteFilepath, localFilepath;

                Log.i("JWP", "downloading: '" + total_file);
                if (total_file > now_targetFile) {
                    Map<String, String> targetFile = targetFiles
                            .get(now_targetFile);

                    remoteFilepath = targetFile.get("link");
                    localFilepath = targetFile.get("putch");
                    Log.i("JWP" + getClass().getName(), "downloading: '"
                            + remoteFilepath + "' => '" + localFilepath + "'");

                    File tempFile = File.createTempFile("jwp_", "_temp",
                            new File(funct.get_dir_app()
                                    + "/downloads/temp/"));

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
                                connection
                                        .setConnectTimeout(7000);
                                connection.setReadTimeout(7000);
                                BufferedInputStream bis = new BufferedInputStream(
                                        connection.getInputStream());
                                FileOutputStream fos = new FileOutputStream(
                                        tempFile);
                                int bytesRead, totalBytesRead = 0;
                                byte[] bytes = new byte[BYTES_BUFFER_SIZE];
                                // String progress, kbytes;
                                while (!isCancelled()
                                        && (bytesRead = bis.read(bytes)) != -1) {
                                    totalBytesRead += bytesRead;
                                    fos.write(bytes, 0, bytesRead);

                                    total_file = get_targetFiles_num();

                                    if (!isCancelled() && loopCount++ % 20 == 0) {
                                        RemoteViews progressView = getProgressView(
                                                now_targetFile + 1, total_file,
                                                totalBytesRead, filesize,
                                                localFile.getName());

                                        if (!isCancelled()) {
                                            showNotification(
                                                    progressView,
                                                    getString(R.string.download_title),
                                                    getApplicationContext());
                                        } else {
                                            showNotification(
                                                    progressView,
                                                    getString(R.string.download_cancelled),
                                                    getApplicationContext());
                                        }
                                    }
                                }

                                fos.close();
                                bis.close();

                                tempFile.renameTo(localFile);

                                if (isCancelled())
                                    return null;
                            } else {
                                Log.i("JWP" + getClass().getName(),
                                        "file size unknown for remote file: "
                                                + remoteFilepath);

                                showNotification_popup(
                                        getString(R.string.download_failed),
                                        getString(R.string.download_title),
                                        "Failed: "
                                                + (new File(remoteFilepath))
                                                .getName(),
                                        getApplicationContext());

                                success = 0;
                            }
                        }
                        success = now_targetFile + 1;
                    } catch (SocketTimeoutException e) {
                        showNotification_popup(
                                getString(R.string.download_failed),
                                getString(R.string.download_title), "Failed: "
                                + (new File(remoteFilepath)).getName(),
                                getApplicationContext());
                        success = 0;

                    } catch (Exception e) {
                        funct.send_bug_report(e);

                        showNotification_popup(
                                getString(R.string.download_failed),
                                getString(R.string.download_title), "Failed: "
                                + (new File(remoteFilepath)).getName(),
                                getApplicationContext());
                        success = 0;
                    }
                    now_targetFile++;
                }
            } catch (Exception e) {
                funct.send_bug_report(e);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.e("JWP" + getClass().getName(), "AsyncTask Cancelled");
            showNotification_popup(getString(R.string.download_cancelled),
                    getString(R.string.download_title),
                    getString(R.string.download_cancelled),
                    getApplicationContext());
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                onFinishDownload(success);
                showNotification_popup(getString(R.string.download_finished),
                        getString(R.string.download_title),
                        getString(R.string.download_finished),
                        getApplicationContext());
            } catch (Exception e) {
                funct.send_bug_report(e);
            }
        }
    }

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
