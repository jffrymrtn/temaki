package com.jmartin.temaki;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Author: Jeff Martin, 2013
 */
public class MainListsFragment extends Fragment {
    private ListView itemsListView;
    private EditText addItemsEditText;
    private ArrayAdapter<String> itemsListAdapter;

    private String listName;
    private ArrayList<String> listItems;
    private MainDrawerActivity parentActivity;

    public MainListsFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        if (savedInstanceState != null) {
            listItems = savedInstanceState.getStringArrayList(parentActivity.LIST_ITEMS_BUNDLE_KEY);
        }

        itemsListView = (ListView) view.findViewById(R.id.mainListView);
        addItemsEditText = (EditText) view.findViewById(R.id.addItemEditText);

        itemsListAdapter = new ArrayAdapter<String>(parentActivity.getApplicationContext(), R.layout.main_list_item, listItems);
        itemsListView.setAdapter(itemsListAdapter);

        addItemsEditText.setOnEditorActionListener(new NewItemsEditTextListener());

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(parentActivity.LIST_ITEMS_BUNDLE_KEY, listItems);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        // Add the current list to the HashMap lists
        parentActivity.saveList(listName, listItems);
        parentActivity.saveListsToSharedPreferences();
        super.onPause();
    }

    /**
     * Initialize the class' parentActivity context.
     * @param parentActivity the parent Activity to use.
     */
    public void initFragment(MainDrawerActivity parentActivity, String listName, ArrayList<String> list) {
        this.parentActivity = parentActivity;
        this.listItems = list;
        this.listName = listName;
    }

    /**
     * @return the String listName that is currently selected.
     */
    public String getListName() {
        return listName;
    }

    /**
     * @return an ArrayList<String> containing the list items currently displayed.
     */
    public ArrayList<String> getListItems() {
        return (ArrayList<String>) listItems.clone();
    }

    private class NewItemsEditTextListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            String newItem = v.getText().toString().trim();
            if ((actionId == EditorInfo.IME_ACTION_DONE) && (newItem.length() > 0)){
                listItems.add(newItem);
                itemsListAdapter.notifyDataSetChanged();
                v.setText("");
                return true;
            }
            return false;
        }
    }
}
