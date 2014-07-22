package ua.pp.dimoshka.classes;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import ua.pp.dimoshka.jwp.R;

public class class_widget extends AppWidgetProvider {

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    final String ACTION_ON_CLICK = "ua.pp.dimoshka.classes.class_widget.itemonclick";
    final static String ITEM_POSITION = "item_position";
    final static String ITEM_LINK = "item_link";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void updateWidget(Context context, AppWidgetManager appWidgetManager,
                      int appWidgetId) {
        RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.widget);
        setUpdateTV(view, context, appWidgetId);
        setList(view, context, appWidgetId);
        setListClick(view, context, appWidgetId);

        appWidgetManager.updateAppWidget(appWidgetId, view);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,
                R.id.lvList);
    }

    void setUpdateTV(RemoteViews view, Context context, int appWidgetId) {
        view.setTextViewText(R.id.tvUpdate,
                sdf.format(new Date(System.currentTimeMillis())));
        Intent updIntent = new Intent(context, class_widget.class);
        updIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                new int[]{appWidgetId});
        PendingIntent updPIntent = PendingIntent.getBroadcast(context,
                appWidgetId, updIntent, 0);
        view.setOnClickPendingIntent(R.id.tvUpdate, updPIntent);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    void setList(RemoteViews rv, Context context, int appWidgetId) {
        Intent adapter = new Intent(context, class_widget_service.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri data = Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME));
        adapter.setData(data);
        rv.setRemoteAdapter(R.id.lvList, adapter);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void setListClick(RemoteViews rv, Context context, int appWidgetId) {
        Intent listClickIntent = new Intent(context, class_widget.class);
        listClickIntent.setAction(ACTION_ON_CLICK);
        PendingIntent listClickPIntent = PendingIntent.getBroadcast(context, 0,
                listClickIntent, 0);
        rv.setPendingIntentTemplate(R.id.lvList, listClickPIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equalsIgnoreCase(ACTION_ON_CLICK)) {
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