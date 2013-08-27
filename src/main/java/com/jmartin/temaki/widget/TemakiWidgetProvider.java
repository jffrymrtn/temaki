package com.jmartin.temaki.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.jmartin.temaki.R;

/**
 * Created by jeff on 2013-08-27.
 */
public class TemakiWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int NUM_WIDGET_INSTANCES = appWidgetIds.length;

        for (int i = 0; i < NUM_WIDGET_INSTANCES; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent widgetIntent = new Intent(context, TemakiWidgetService.class);
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            widgetIntent.setData(Uri.parse(widgetIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
            widgetView.setRemoteAdapter(R.id.widget_list_view, widgetIntent);
            appWidgetManager.updateAppWidget(appWidgetId, widgetView);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
