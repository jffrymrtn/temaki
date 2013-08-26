package com.jmartin.temaki.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jmartin.temaki.R;

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

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View drawerRowView = null;
        TextView listNameTextView;

        if (itemsCount != -1) {
            drawerRowView = inflater.inflate(R.layout.drawer_list_item, parent, false);
            TextView itemsCountTextView = (TextView) drawerRowView.findViewById(R.id.items_count);
            itemsCountTextView.setText(String.valueOf(itemsCount));
        } else {
            drawerRowView = inflater.inflate(R.layout.drawer_list_category_item, parent, false);
            ImageView categoryExpanderImageView = (ImageView) drawerRowView.findViewById(R.id.expander);

            categoryExpanderImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO handle expand/collapse
                }
            });
        }

        listNameTextView = (TextView) drawerRowView.findViewById(R.id.list_name);
        listNameTextView.setText(listName);

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
