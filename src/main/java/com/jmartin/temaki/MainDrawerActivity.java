package com.jmartin.temaki;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Jeff Martin, 2013
 */
public class MainDrawerActivity extends Activity {

    private final String DEFAULT_LIST_NAME = "NEW LIST ";
    protected final String LISTS_SP_KEY = "MAIN_LISTS";
    protected final String LIST_ITEMS_BUNDLE_KEY = "ListItems";

    private DrawerLayout listsDrawerLayout;
    private ListView listsDrawerListView;
    private ActionBarDrawerToggle listsDrawerToggle;
    private ArrayList<String> drawerItems;
    private ArrayAdapter<String> drawerListAdapter;
    private HashMap<String, ArrayList<String>> lists;

    private MainListsFragment mainListsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.main_drawer_layout);

        drawerItems = new ArrayList<String>();

        if (savedInstanceState == null) {
            // Load from SharedPreferences
            SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
            String listsJson = sharedPrefs.getString(LISTS_SP_KEY, "");

            Type listsType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
            lists = new Gson().fromJson(listsJson, listsType);

            if (lists != null && lists.size() > 0) {
                drawerItems.addAll(lists.keySet());
            } else if (lists == null) {
                lists = new HashMap<String, ArrayList<String>>();
            }
        } else {
            // Load from savedInstanceState
            // TODO
//            savedInstanceState.getString();
        }

        // Set the Navigation Drawer up
        listsDrawerLayout = (DrawerLayout) findViewById(R.id.lists_drawer_layout);
        listsDrawerListView = (ListView) findViewById(R.id.lists_drawer);

        // Set the drawer ListView Header
        View drawerListViewHeaderView = getLayoutInflater().inflate(R.layout.drawer_header, null);
        listsDrawerListView.addHeaderView(drawerListViewHeaderView);

        // Set drawer ListView adapter
        drawerListAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems);
        listsDrawerListView.setAdapter(drawerListAdapter);
        listsDrawerListView.setOnItemClickListener(new ListsDrawerClickListener());

        // Set up the ActionBar Drawer Toggle
        listsDrawerToggle = new ActionBarDrawerToggle(this, listsDrawerLayout,R.drawable.ic_drawer,
                                                      R.string.open_drawer, R.string.close_drawer) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(getTitle());
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View view) {
                getActionBar().setTitle(getTitle());
                invalidateOptionsMenu();
            }
        };
        listsDrawerLayout.setDrawerListener(listsDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Load the main fragment with an empty list
        loadList(null, null);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        listsDrawerToggle.syncState();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        listsDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (listsDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadList(String listName, ArrayList<String> list) {
        if (listName == null || list == null) {
            listName = "";
            list = new ArrayList<String>();
        }

        mainListsFragment = new MainListsFragment();
        mainListsFragment.initFragment(this, listName, list);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame_layout, mainListsFragment)
                .commit();
    }

    public void saveListsToSharedPreferences() {
        SharedPreferences.Editor sharedPrefsEditor = getPreferences(MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String listsJson = gson.toJson(lists);
        sharedPrefsEditor.putString(LISTS_SP_KEY, listsJson);
        sharedPrefsEditor.commit();
    }

    private void createNewList() {
        // TODO Show dialog for the name of the list, check for duplicates on drawerItems
        String newListName = "CREATE NEW LIST TEST";
        ArrayList<String> newList = new ArrayList<String>();

        updateDrawer(newListName);
        lists.put(newListName, newList);
        loadList(newListName, newList);

        setTitle(newListName);
    }

    private void updateDrawer(String listName) {
        if (!drawerItems.contains(listName)) {
            drawerItems.add(listName);
            drawerListAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Add the list listItems with name listName to the HashMap of lists.
     * @param listName the name of the new list to add or replace.
     * @param listItems the ArrayList to add or replace.
     */
    public void saveList(String listName, ArrayList<String> listItems) {
        if (listName.length() == 0) {
            listName = DEFAULT_LIST_NAME + lists.size() + 1; // Offset index 0
        }

        if (listItems.size() > 0) {
            lists.put(listName, listItems);
            updateDrawer(listName);
        }
    }

    private class ListsDrawerClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Offset position by 1 because of the header (header @ index 0)
            if (--position < 0) {
                createNewList();
            } else {
                // Load the list specified by position 'position' on the nav drawer
                String listName = drawerItems.get(position);
                loadList(listName, lists.get(listName));
                setTitle(listName);
            }
            // Close the nav drawer
            listsDrawerListView.setItemChecked(position, true);
            listsDrawerLayout.closeDrawer(listsDrawerListView);
        }
    }
}
