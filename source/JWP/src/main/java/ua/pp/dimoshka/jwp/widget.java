package ua.pp.dimoshka.jwp;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;

import ua.pp.dimoshka.classes.class_functions;
import ua.pp.dimoshka.classes.class_rss_news;
import ua.pp.dimoshka.classes.class_sqlite;
import ua.pp.dimoshka.classes.class_widget_service;

public class widget extends AppWidgetProvider {

    public static final String ACTION_ON_UPDATEOK = "ua.pp.dimoshka.jwp.widget_updateok";
    public static final String IDWIDGET = "idwidget";
    public final static String ITEM_POSITION = "item_position";
    public final static String ITEM_LINK = "item_link";
    private static final String ACTION_ON_LISTCLICK = "ua.pp.dimoshka.jwp.widget_listitemonclick";
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public widget() {
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    void updateWidget(Context context, AppWidgetManager appWidgetManager,
                      int appWidgetId) {
        RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.widget);
        setUpdateTV(view, context, appWidgetId);
        setList(view, context, appWidgetId);
        setListClick(view, context, appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, view);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                R.id.list);
    }

    void setUpdateTV(RemoteViews view, Context context, int appWidgetId) {
        //view.setTextViewText(R.id.tvUpdate, context.getString(R.string.download_rss));
        update_rss_news(context, appWidgetId);
        Intent updIntent = new Intent(context, widget.class);
        updIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
        PendingIntent updPIntent = PendingIntent.getBroadcast(context,
                appWidgetId, updIntent, 0);
        view.setOnClickPendingIntent(R.id.tvUpdate, updPIntent);
        Intent intentapp = new Intent(context, main.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentapp, 0);
        view.setOnClickPendingIntent(R.id.image, pendingIntent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setList(RemoteViews rv, Context context, int appWidgetId) {
        Intent adapter = new Intent(context, class_widget_service.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME));
        adapter.setData(data);
        rv.setRemoteAdapter(R.id.list, adapter);
    }

    void setListClick(RemoteViews rv, Context context, int appWidgetId) {
        Intent listClickIntent = new Intent(context, widget.class);
        listClickIntent.setAction(ACTION_ON_LISTCLICK);
        PendingIntent listClickPIntent = PendingIntent.getBroadcast(context, appWidgetId,
                listClickIntent, 0);
        rv.setPendingIntentTemplate(R.id.list, listClickPIntent);
    }

    private void update_rss_news(Context context, int appWidgetId) {
        class_functions funct = new class_functions(context);
        class_sqlite dbOpenHelper = new class_sqlite(context);
        SQLiteDatabase database = dbOpenHelper.openDataBase();
        class_rss_news rss_news = new class_rss_news(context, database, funct);
        rss_news.get_all_feeds_widget(appWidgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equalsIgnoreCase(ACTION_ON_UPDATEOK)) {
            Log.d("WIDGET", "updated afte load rss");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, widget.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.list);
        } else if (intent.getAction().equalsIgnoreCase(ACTION_ON_LISTCLICK)) {
            int itemPos = intent.getIntExtra(ITEM_POSITION, -1);
            String itemLink = intent.getStringExtra(ITEM_LINK);
            if (itemPos != -1 && itemLink.length() > 0) {
                Intent intent_news = new Intent();
                intent_news.setAction(android.content.Intent.ACTION_VIEW);
                intent_news.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri data = Uri.parse(itemLink);
                intent_news.setDataAndType(data, "text/html");
                context.startActivity(intent_news);
            }
        }
    }
}