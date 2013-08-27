package com.jmartin.temaki.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.jmartin.temaki.R;

import java.util.ArrayList;

/**
 * Created by jeff on 2013-08-27.
 */
public class TemakiWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TemakiWidgetFactory(this.getApplicationContext(), intent);
    }

    private class TemakiWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context context;
//        private int widgetId;

        private ArrayList<String> widgetItems = new ArrayList<String>();

        public TemakiWidgetFactory(Context context, Intent widgetIntent) {
            this.context = context;
//            this.widgetId = widgetIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            widgetItems.add("A");
            widgetItems.add("B");
            widgetItems.add("C");
            widgetItems.add("D");
            widgetItems.add("E");
            widgetItems.add("F");
            widgetItems.add("G");
            widgetItems.add("H");
            widgetItems.add("I");
        }

        @Override
        public void onDataSetChanged() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return widgetItems.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main_list_item);
            views.setTextViewText(R.id.main_list_item, widgetItems.get(position));
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return widgetItems.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
