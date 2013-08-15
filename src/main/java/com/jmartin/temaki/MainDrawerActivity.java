package com.jmartin.temaki;

import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jmartin.temaki.dialog.GenericAlertDialog;
import com.jmartin.temaki.dialog.GenericInputDialog;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Author: Jeff Martin, 2013
 */
public class MainDrawerActivity extends FragmentActivity
        implements GenericInputDialog.GenericInputDialogListener, GenericAlertDialog.GenericAlertDialogListener {

    private final String LIST_ITEMS_BUNDLE_KEY = "ListItems";
    private final String LIST_NAME_BUNDLE_KEY = "ListName";
    private final String NEW_LIST_DIALOG_TITLE = "Enter a name for the new list:";
    public static final String CONFIRM_DELETE_DIALOG_TITLE = "Are you sure you want to delete this?";
    private final String DEFAULT_LIST_NAME = "NEW LIST ";
    protected final String LISTS_SP_KEY = "MAIN_LISTS";

    private DrawerLayout listsDrawerLayout;
    private ListView listsDrawerListView;
    private ActionBarDrawerToggle listsDrawerToggle;
    private ArrayList<String> drawerItems;
    private ArrayAdapter<String> drawerListAdapter;
    private HashMap<String, ArrayList<String>> lists;

    private MainListsFragment mainListsFragment;

    /* Used for keeping track of selected item. Ideally don't want to do it this way but isSelected
    * is not working in the clicklistener below.*/
    private int selectedItemPos = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.main_drawer_layout);

        drawerItems = new ArrayList<String>();

        String listsJson = "";
        String loadedListName = null;
        ArrayList<String> loadedList = null;

        if (savedInstanceState == null) {
            // Load from SharedPreferences
            SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
            listsJson = sharedPrefs.getString(LISTS_SP_KEY, "");
        } else {
            // load from savedInstanceState
            listsJson = savedInstanceState.getString(LISTS_SP_KEY, "");
            loadedListName = savedInstanceState.getString(LIST_NAME_BUNDLE_KEY);
            loadedList = savedInstanceState.getStringArrayList(LIST_ITEMS_BUNDLE_KEY);
        }

        // Initialize lists variable
        Type listsType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
        lists = new Gson().fromJson(listsJson, listsType);

        if (lists != null && lists.size() > 0) {
            drawerItems.addAll(lists.keySet());
        } else if (lists == null) {
            lists = new HashMap<String, ArrayList<String>>();
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

        // NavigationBar shadow
        listsDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

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
        mainListsFragment = new MainListsFragment();
        loadListIntoFragment(loadedListName, loadedList);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame_layout, mainListsFragment)
                .commit();

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
    protected void onSaveInstanceState(Bundle outState) {
        // Store drawersList
        Gson gson = new Gson();
        String jsonLists = gson.toJson(lists);
        outState.putString(LISTS_SP_KEY, jsonLists);

        outState.putString(LIST_NAME_BUNDLE_KEY, mainListsFragment.getListName());
        outState.putStringArrayList(LIST_ITEMS_BUNDLE_KEY, mainListsFragment.getListItems());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (listsDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_delete_list:
                deleteLoadedList();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFinishAlertDialog() {
        lists.remove(drawerItems.get(selectedItemPos));
        drawerItems.remove(selectedItemPos);

        drawerListAdapter.notifyDataSetChanged();

        // Reload
        loadListIntoFragment(null, null);
    }

    @Override
    public void onFinishDialog(String newListName) {
        if (newListName.equalsIgnoreCase("")) {
            newListName = getDefaultTitle();
        }

        if (!lists.containsKey(newListName)) {
            updateDrawer(newListName);
            lists.put(newListName, new ArrayList<String>());
        }

        loadListIntoFragment(newListName, lists.get(newListName));
    }

    @Override
    public void onPause() {
        // Add the current list to the HashMap lists
        saveList(mainListsFragment.getListName(), mainListsFragment.getListItems());
        saveListsToSharedPreferences();
        super.onPause();
    }

    /**
     * Delete the currently checked list on the Navigation Drawer.
     */
    private void deleteLoadedList() {
        showDeleteListConfirmationDialog();
    }

    /**
     * Load the list 'list' with name 'listName'.
     * @param listName the name of the list to load.
     * @param list the list to load.
     */
    public void loadListIntoFragment(String listName, ArrayList<String> list) {
        if (listName == null || list == null) {
            listName = getDefaultTitle();
            list = new ArrayList<String>();
        }

        mainListsFragment.loadList(listName, list);
    }

    /**
     * Save the current list of lists to SharedPreferences.
     */
    public void saveListsToSharedPreferences() {
        SharedPreferences.Editor sharedPrefsEditor = getPreferences(MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String listsJson = gson.toJson(lists);
        sharedPrefsEditor.putString(LISTS_SP_KEY, listsJson);
        sharedPrefsEditor.commit();
    }

    /**
     * Prompt the user for the name of a list to be created.
     */
    private void createNewList() {
        // Show dialog for the name of the list, check for duplicates on drawerItems
        showNewListDialog();
    }

    /**
     * Show the New List prompt dialog.
     */
    private void showNewListDialog() {
        FragmentManager fragManager = getFragmentManager();
        GenericInputDialog dialog = new GenericInputDialog();
        dialog.setTitle(NEW_LIST_DIALOG_TITLE);
        dialog.show(fragManager, "generic_name_dialog_fragment");
    }

    /**
     * Show the Delete List prompt dialog.
     */
    private void showDeleteListConfirmationDialog() {
        FragmentManager fragManager = getFragmentManager();
        GenericAlertDialog dialog = new GenericAlertDialog();
        dialog.setTitle(CONFIRM_DELETE_DIALOG_TITLE);
        dialog.show(fragManager, "generic_alert_dialog_fragment");
    }


    /**
     * Update the Navigation Drawer ListView with the new list listName.
     * @param listName the new list to add to the Navigation Drawer.
     */
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
        if (listName == null || listItems == null) return;

        if (listName.length() == 0) {
            listName = getDefaultTitle();
        }

        if (listItems.size() > 0) {
            lists.put(listName, listItems);
            updateDrawer(listName);
        }
    }

    /**
     * @return the default title for a new list.
     */
    private String getDefaultTitle() {
        return DEFAULT_LIST_NAME + (lists.size() + 1); // Offset index 0
    }

    private class ListsDrawerClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Before loading a new list, make sure the currently loaded one is saved
            saveList(mainListsFragment.getListName(), mainListsFragment.getListItems());

            // Offset position by 1 because of the header (header @ index 0)
            if (--position < 0) {
                createNewList();
            } else {
                // Load the list specified by position 'position' on the nav drawer
                String listName = drawerItems.get(position);
                loadListIntoFragment(listName, lists.get(listName));
            }

            // Keep track of the currently loaded list
            selectedItemPos = position;

            // Close the nav drawer
            view.setSelected(true);
            listsDrawerLayout.closeDrawer(listsDrawerListView);
        }
    }
}
