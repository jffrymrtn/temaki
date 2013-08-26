package com.jmartin.temaki.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.jmartin.temaki.R;
import com.jmartin.temaki.settings.SettingsActivity;

import java.util.ArrayList;

/**
 * Created by jeff on 2013-08-24.
 */
public class ListItemsAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final ArrayList<String> data;
    private int selectedItemPosition;

    public ListItemsAdapter(Context context, int resource, ArrayList<String> items) {
        super(context, resource, items);

        this.context = context;
        this.data = items;
        this.selectedItemPosition = -1;
    }

    public void setSelectedItemPosition(int position) {
        this.selectedItemPosition = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.main_list_item, parent, false);
        TextView rowTextView = (TextView) row.findViewById(R.id.main_list_item);

        SharedPreferences prefMgr = PreferenceManager.getDefaultSharedPreferences(context);

        String itemText = getItem(position);

        // If user wants to force auto-capitalization, make sure first letters are capitalized
        if (prefMgr.getBoolean(SettingsActivity.KEY_PREF_LIST_ITEMS_CAPITALIZE_OPTION, true)) {
            itemText = itemText.substring(0, 1).toUpperCase() + itemText.substring(1);
        }

        rowTextView.setText(itemText);

        if (position == selectedItemPosition) {
            row.setBackgroundResource(R.drawable.main_list_item_selected);
        } else {
            row.setBackgroundResource(R.drawable.main_list_item);
        }
        return row;
    }
}
