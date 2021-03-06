package ua.pp.dimoshka.classes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.pp.dimoshka.jwp.R;

public class service_downloads_files extends Service {

    private static final int SERVICE_ID = 0x101104;
    private static final int BYTES_BUFFER_SIZE = 2 * 1024;
    private final IBinder binder = new FileDownloadBinder();
    private class_functions funct = null;
    private NotificationManager notificationManager = null;
    private AsyncDownloadTask task = null;
    private boolean isRunning = false;
    private ArrayList<Map<String, String>> targetFiles = null;
    private int now_targetFile = 0;

    @Override
    public void onCreate() {
        if (isRunning)
            return;
        else
            funct = new class_functions(getBaseContext());
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
            Map<String, String> targetFile = new HashMap<String, String>();
            targetFile.put("link", intent.getStringExtra("file_url"));
            targetFile.put("putch", intent.getStringExtra("file_putch"));
            targetFile.put("img", intent.getStringExtra("img_putch"));
            targetFiles.add(targetFile);

            task = new AsyncDownloadTask();
            task.execute();
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
        return START_STICKY;
    }

    ArrayList<Map<String, String>> get_targetFiles() {
        return targetFiles;
    }

    int get_targetFiles_num() {
        return targetFiles.size();
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll();
        Log.d("JWP" + getClass().getName(), "Service Cancelled");
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

    void onFinishDownload(int success) {
        try {
            if (success > 0) {
                Map<String, String> targetFile = targetFiles.get(success - 1);
                File localFile = new File(targetFile.get("putch"));
                if (localFile.exists()) {
                    Log.d("JWP" + getClass().getName(), "Update to 1 - "
                            + localFile.getName());
                    class_sqlite dbOpenHelper = new class_sqlite(getBaseContext());
                    SQLiteDatabase database = dbOpenHelper.openDataBase();
                    funct.update_file_isn(database, localFile.getName(), Integer.valueOf(1));
                    dbOpenHelper.close();
                    //funct.send_to_local_brodcast("update", null);
                    funct.send_to_local_brodcast("loading", new HashMap<String, Integer>() {{
                        put("status", 3);
                    }});
                }
            }
        } catch (Exception e) {
            funct.send_bug_report(e);
        }
    }

    int getNotificationIcon() {
        return R.drawable.ic_launcher;
    }

    RemoteViews getProgressView(int currentNumFile,
                                int totalNumFiles, int currentReceivedBytes, int totalNumBytes,
                                String filename, String imgpatch) {
        RemoteViews contentView = new RemoteViews(getPackageName(),
                R.layout.notification);

        try {
            if (imgpatch.length() > 0) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(imgpatch, options);
                contentView.setImageViewBitmap(R.id.image, bitmap);
            } else contentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
        } catch (Exception ex) {
            contentView.setImageViewResource(R.id.image, R.drawable.ic_launcher);
        }

        contentView.setTextViewText(R.id.text1, filename);
        contentView.setProgressBar(R.id.progress, 100, 100
                * currentReceivedBytes / totalNumBytes, false);
        contentView.setTextViewText(R.id.text2,
                String.format("(%d / %d)", Integer.valueOf(currentNumFile), Integer.valueOf(totalNumFiles)));
        contentView.setTextViewText(R.id.text3, getStringByteSize(currentReceivedBytes) + " / " + getStringByteSize(totalNumBytes));
        return contentView;
    }

    void showNotification_popup(String ticker, String title, String text, Context context) {
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                .setSmallIcon(getNotificationIcon())
                .setAutoCancel(true)
                .setTicker(ticker)
                .setContentText(text)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setDefaults(Notification.DEFAULT_ALL);
        Notification notification = nb.build();
        notificationManager.notify(SERVICE_ID, notification);
    }

    void showNotification(RemoteViews remoteView, String ticker, Context context) {
        NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                .setContentTitle(ticker)
                .setContentText(ticker)
                .setSmallIcon(getNotificationIcon())
                .setLargeIcon(null)
                .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT))
                .setAutoCancel(false)
                .setContent(remoteView);
        Notification notification = nb.build();
        notificationManager.notify(SERVICE_ID, notification);
    }

    protected int getConnectTimeout() {
        return 10000;
    }

    protected int getReadTimeout() {
        return 10000;
    }

    protected String getStringByteSize(int size) {
        if (size > 1024 * 1024) // mega
        {
            return String.format("%.1f MB", Float.valueOf(size / (float) (1024 * 1024)));
        } else if (size > 1024) // kilo
        {
            return String.format("%.1f KB", Float.valueOf(size / 1024.0f));
        } else {
            return String.format("%d B");
        }
    }

    private class FileDownloadBinder extends Binder {
        service_downloads_files getService() {
            return service_downloads_files.this;
        }
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


        @Override
        protected Void doInBackground(Void[] params) {
            try {
                String remoteFilepath, localFilepath, imgFilepath;
                boolean redirect = false;
                int filesize = -1;

                if (isCancelled()) return null;

                Log.i("JWP", "downloading: '" + total_file);
                if (total_file > now_targetFile) {
                    Map<String, String> targetFile = targetFiles
                            .get(now_targetFile);

                    remoteFilepath = targetFile.get("link");
                    localFilepath = targetFile.get("putch");
                    imgFilepath = targetFile.get("img");

                    Log.i("JWP" + getClass().getName(), "downloading: '"
                            + remoteFilepath + "' => '" + localFilepath + "'");

                    File tempFile = File.createTempFile("jwp_", "_temp",
                            new File(funct.get_dir_app()
                                    + "/downloads/temp/")
                    );


                    File localFile = new File(localFilepath);
                    if (!localFile.exists()) {
                        URL obj = new URL(remoteFilepath);
                        HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
                        conn.setReadTimeout(5000);
                        conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                        conn.addRequestProperty("User-Agent", "Mozilla");
                        //conn.addRequestProperty("Referer", "google.com");

                        try {

                            int status = conn.getResponseCode();
                            if (status != HttpURLConnection.HTTP_OK) {
                                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                                        || status == HttpURLConnection.HTTP_MOVED_PERM
                                        || status == HttpURLConnection.HTTP_SEE_OTHER)
                                    redirect = true;
                            }

                            Log.d("Response Code ... ", status + "");


                            if (redirect) {
                                String newUrl = conn.getHeaderField("Location");
                                String cookies = conn.getHeaderField("Set-Cookie");
                                conn = (HttpURLConnection) new URL(newUrl).openConnection();
                                conn.setRequestProperty("Cookie", cookies);
                                conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                                conn.addRequestProperty("User-Agent", "Mozilla");
                                conn.addRequestProperty("Referer", "google.com");
                                Log.d("Redirect to URL : ", newUrl);
                            }


                            try {
                                filesize = conn.getContentLength();
                            } catch (Exception e) {
                                filesize = -1;
                            }

                            Log.d("filesize : ", filesize + "");

                            int loopCount = 0;
                            if (filesize > 0) {

                                BufferedInputStream bis = new BufferedInputStream(
                                        conn.getInputStream());
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

                                    if (!isCancelled() && loopCount % 20 == 0) {
                                        RemoteViews progressView = getProgressView(
                                                now_targetFile + 1, total_file,
                                                totalBytesRead, filesize,
                                                localFile.getName(), imgFilepath);

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
                                    loopCount++;
                                }

                                fos.close();
                                bis.close();

                                tempFile.renameTo(localFile);

                                if (isCancelled())
                                    return null;
                            } else {
                                Log.e("JWP" + getClass().getName(),
                                        "file size unknown for remote file: "
                                                + remoteFilepath
                                );

                                showNotification_popup(
                                        getString(R.string.download_failed),
                                        getString(R.string.download_title),
                                        "Failed: "
                                                + (new File(remoteFilepath))
                                                .getName(),
                                        getApplicationContext()
                                );

                                success = 0;
                            }

                            success = now_targetFile + 1;

                        } catch (SocketTimeoutException e) {
                            showNotification_popup(
                                    getString(R.string.download_failed),
                                    getString(R.string.download_title), "Failed: "
                                            + (new File(remoteFilepath)).getName(),
                                    getApplicationContext()
                            );
                            success = 0;

                        } catch (Exception e) {
                            funct.send_bug_report(e);

                            showNotification_popup(
                                    getString(R.string.download_failed),
                                    getString(R.string.download_title), "Failed: "
                                            + (new File(remoteFilepath)).getName(),
                                    getApplicationContext()
                            );
                            success = 0;
                        } finally {
                            conn.disconnect();
                        }

                    }
                    now_targetFile++;
                }
            } catch (SocketTimeoutException e) {
                //funct.send_bug_report(e);
            } catch (UnknownHostException e) {
                //funct.send_bug_report(e);
            } catch (Exception e) {
                funct.send_bug_report(e);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d("JWP" + getClass().getName(), "AsyncTask Cancelled");
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

}
