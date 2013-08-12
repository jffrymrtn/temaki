package com.jmartin.temaki;

import android.app.Activity;
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
 * Created by jeff on 2013-08-11.
 */
public class MainListsFragment extends Fragment {
    public static final String LIST_ITEMS_BUNDLE_KEY = "ListItems";
    private ListView itemsListView;
    private EditText addItemsEditText;
    private ArrayAdapter<String> itemsListAdapter;

    private ArrayList<String> listItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        if (savedInstanceState == null) {
            listItems = new ArrayList<String>();
        } else {
            listItems = savedInstanceState.getStringArrayList(LIST_ITEMS_BUNDLE_KEY);
        }

        itemsListView = (ListView) view.findViewById(R.id.mainListView);
        addItemsEditText = (EditText) view.findViewById(R.id.addItemEditText);

        itemsListAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.main_list_item, listItems);
        itemsListView.setAdapter(itemsListAdapter);

        addItemsEditText.setOnEditorActionListener(new NewItemsEditTextListener());

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList(LIST_ITEMS_BUNDLE_KEY, listItems);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        Activity activity = getActivity();
        SharedPreferences sharedPreferences = activity.getPreferences(activity.getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor spEdit = sharedPreferences.edit();


        // Serialize into Gson
        //spEdit.putStringSet(LIST_ITEMS_BUNDLE_KEY, )
        super.onDestroy();
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
