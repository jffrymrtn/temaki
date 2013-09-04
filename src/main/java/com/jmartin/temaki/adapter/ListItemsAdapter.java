package com.jmartin.temaki.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.jmartin.temaki.R;
import com.jmartin.temaki.model.Constants;
import com.jmartin.temaki.model.TemakiItem;
import com.jmartin.temaki.settings.SettingsFragment;

import java.util.ArrayList;

/**
 * Created by jeff on 2013-08-24.
 */
public class ListItemsAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private final ArrayList<TemakiItem> data;
    private  ArrayList<TemakiItem> filteredData;
    private int selectedItemPosition;

    public ListItemsAdapter(Context context, ArrayList<TemakiItem> items) {
        this.context = context;
        this.data = items;
        this.filteredData = items;
        this.selectedItemPosition = -1;
    }

    public void setSelectedItemPosition(int position) {
        this.selectedItemPosition = position;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.main_list_item, parent, false);
        TextView rowTextView = (TextView) row.findViewById(R.id.main_list_item);

        SharedPreferences prefMgr = PreferenceManager.getDefaultSharedPreferences(context);

        TemakiItem item = (TemakiItem) getItem(position);
        String itemText = item.getText();

        // If user wants to force auto-capitalization, make sure first letters are capitalized
        if (prefMgr.getBoolean(Constants.KEY_PREF_LIST_ITEMS_CAPITALIZE_OPTION, true)) {
            itemText = itemText.substring(0, 1).toUpperCase() + itemText.substring(1);
        }

        rowTextView.setText(itemText);

        if (item.isHighlighted()) {
            rowTextView.setTypeface(null, Typeface.BOLD);
            rowTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_highlight, 0);
        } else {
            rowTextView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            rowTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        // Item is marked as finished
        if (item.isFinished()) {
            rowTextView.setPaintFlags(rowTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            rowTextView.setTextColor(context.getResources().getColor(R.color.main_item_done_text_color));
        } else {
            rowTextView.setPaintFlags(rowTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            rowTextView.setTextColor(context.getResources().getColor(R.color.main_dark_text_color));
        }

        // Make sure the selection acts properly when scrolling
        if (position == selectedItemPosition) {
            row.setBackgroundResource(R.drawable.main_list_item_selected);
        } else {
            if (item.isFinished()) {
                row.setBackgroundResource(R.drawable.main_list_item_finished);
            } else {
                row.setBackgroundResource(R.drawable.main_list_item);
            }
        }
        return row;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults searchResults = new FilterResults();

                if (constraint == null || constraint.length() == 0) {
                    searchResults.values = data;
                    searchResults.count = data.size();
                } else {
                    ArrayList<TemakiItem> searchResultsData = new ArrayList<TemakiItem>();

                    for (TemakiItem item : data) {
                        if (item.getText().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            searchResultsData.add(item);
                        }
                    }

                    searchResults.values = searchResultsData;
                    searchResults.count = searchResultsData.size();
                }
                return searchResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredData = (ArrayList<TemakiItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}
