package com.jmartin.temaki.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jmartin.temaki.R;
import com.jmartin.temaki.model.TemakiItem;

import java.util.LinkedHashMap;

/**
 * Created by jeff on 2013-08-23.
 */
public class DrawerListAdapter extends BaseAdapter {

    private Context context;
    private LinkedHashMap<String, Integer> lists;
    private String[] keys;

    public DrawerListAdapter(Context context, LinkedHashMap<String, Integer> lists) {
        this.context = context;
        this.lists = lists;

        this.keys = lists.keySet().toArray(new String[lists.size()]);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Integer getItem(int position) {
        return lists.get(keys[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String listName = keys[position];
        int itemsCount = getItem(position);

        String theme = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_theme_key), "");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View drawerRowView = inflater.inflate(R.layout.drawer_list_item, parent, false);

        TextView itemsCountTextView = (TextView) drawerRowView.findViewById(R.id.items_count);
        itemsCountTextView.setText(String.valueOf(itemsCount));

        TextView listNameTextView = (TextView) drawerRowView.findViewById(R.id.list_name);
        listNameTextView.setText(listName);

        if (!theme.equals("")) {
            if (theme.equals(context.getString(R.string.theme_dark))) {
                itemsCountTextView.setTextColor(context.getResources().getColor(android.R.color.white));
                listNameTextView.setTextColor(context.getResources().getColor(android.R.color.white));
            } else {
                itemsCountTextView.setTextColor(context.getResources().getColor(R.color.dark_grey));
                listNameTextView.setTextColor(context.getResources().getColor(R.color.dark_grey));
            }
        }

        return drawerRowView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        this.keys = lists.keySet().toArray(new String[lists.size()]);
    }

    public String getKeyAtPosition(int position) {
        return keys[position];
    }
}
