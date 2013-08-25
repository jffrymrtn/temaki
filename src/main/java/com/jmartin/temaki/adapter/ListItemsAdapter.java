package com.jmartin.temaki.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jmartin.temaki.R;

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

    public void setSelectionItemPosition(int position) {
        this.selectedItemPosition = position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.main_list_item, parent, false);
        TextView rowTextView = (TextView) row.findViewById(R.id.main_list_item);

        rowTextView.setText(data.get(position));

        if (position == selectedItemPosition) {
            row.setBackgroundResource(R.drawable.main_list_item_selected);
        } else {
            row.setBackgroundResource(R.drawable.main_list_item);
        }
        return row;
    }
}
