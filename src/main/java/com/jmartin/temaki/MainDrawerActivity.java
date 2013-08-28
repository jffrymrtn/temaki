package com.jmartin.temaki;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jmartin.temaki.adapter.DrawerListAdapter;
import com.jmartin.temaki.dialog.DeleteConfirmationDialog;
import com.jmartin.temaki.dialog.GenericInputDialog;
import com.jmartin.temaki.settings.SettingsActivity;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Author: Jeff Martin, 2013
 */
public class MainDrawerActivity extends FragmentActivity
        implements DeleteConfirmationDialog.GenericAlertDialogListener {

    private final int EMPTY_LIST_ITEMS_COUNT = 0;
    private final int LIST_CATEGORY_DEFAULT_COUNT = -1;

    private final int NEW_LIST_ID = 0;
    private final int RENAME_LIST_ID = 1;
    private final int NEW_CATEGORY_ID = 2;

    private final String ALERT_DIALOG_TAG = "delete_confirmation_dialog_fragment";
    private final String INPUT_DIALOG_TAG = "generic_name_dialog_fragment";
    private final String LIST_ITEMS_BUNDLE_KEY = "ListItems";
    private final String LIST_NAME_BUNDLE_KEY = "ListName";

    private final String LIST_NAME_DIALOG_TITLE = "Enter this list's name:";
    private final String CATEGORY_DIALOG_TITLE = "Enter the category's name:";
    public static final String CONFIRM_DELETE_DIALOG_TITLE = "Delete this List?";
    private final String DEFAULT_LIST_NAME = "NEW LIST ";
    protected final String LISTS_SP_KEY = "MAIN_LISTS";
    private final String LAST_OPENED_LIST_SP_KEY = "last_opened_list";

    private GenericInputDialog inputDialog;
    private DeleteConfirmationDialog alertDialog;

    private DrawerLayout listsDrawerLayout;
    private ListView listsDrawerListView;
    private ActionBarDrawerToggle listsDrawerToggle;
    private LinkedHashMap<String, Integer> drawerItems;
    private DrawerListAdapter drawerListAdapter;
    private HashMap<String, ArrayList<String>> lists;

    private MainListsFragment mainListsFragment;
    private SearchView searchView;

    /* Used for keeping track of selected item. Ideally don't want to do it this way but isSelected
    * is not working in the click listener below.*/
    private String selectedListName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.main_drawer_layout);

        drawerItems = new LinkedHashMap<String, Integer>();

        String listsJson;
        String loadedListName = "";
        ArrayList<String> loadedList = null;

        if (savedInstanceState == null) {
            // Load from SharedPreferences
            SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
            listsJson = sharedPrefs.getString(LISTS_SP_KEY, "");

            // Load the last loaded list if needed
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_PREF_STARTUP_OPTION, false)) {
                loadedListName = sharedPrefs.getString(LAST_OPENED_LIST_SP_KEY, "");
            }
        } else {
            // load from savedInstanceState
            listsJson = savedInstanceState.getString(LISTS_SP_KEY, "");
            loadedListName = savedInstanceState.getString(LIST_NAME_BUNDLE_KEY, "");
            loadedList = savedInstanceState.getStringArrayList(LIST_ITEMS_BUNDLE_KEY);
        }

        // Initialize lists variable
        deserializeJsonLists(listsJson);

        // If there is a list to load, load it
        if (!loadedListName.equalsIgnoreCase("")) {
            loadedList = lists.get(loadedListName);
            selectedListName = loadedListName;
        }

        // Set the Navigation Drawer up
        listsDrawerLayout = (DrawerLayout) findViewById(R.id.lists_drawer_layout);
        listsDrawerListView = (ListView) findViewById(R.id.lists_drawer);

        // Set the drawer ListView Header
        View drawerListViewHeaderView = getLayoutInflater().inflate(R.layout.drawer_newlist_header, null);
        listsDrawerListView.addHeaderView(drawerListViewHeaderView);

        // Set drawer ListView adapter
        drawerListAdapter = new DrawerListAdapter(this, drawerItems);
        listsDrawerListView.setAdapter(drawerListAdapter);
        listsDrawerListView.setOnItemClickListener(new ListsDrawerClickListener());

        // NavigationBar shadow
        listsDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // Set up the ActionBar Drawer Toggle
        listsDrawerToggle = new ActionBarDrawerToggle(this, listsDrawerLayout,R.drawable.ic_drawer,
                                                      R.string.open_drawer, R.string.close_drawer) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mainListsFragment.getListName());
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View view) {
                getActionBar().setTitle(getTitle());
                hideKeyboard();
                searchView.clearFocus();
                invalidateOptionsMenu();

                // Make sure the navigation bar shows the updated count of the currently opened
                // list - only if the list already exists (not new), OR if it has items
                if ((drawerItems.containsKey(mainListsFragment.getListName())) ||
                    (mainListsFragment.getListItems().size() > 0)) {
                    updateDrawer(mainListsFragment.getListName(), mainListsFragment.getListItems().size());
                }
            }
        };

        listsDrawerLayout.setDrawerListener(listsDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // Set up Preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Load the main fragment with an empty list
        mainListsFragment = new MainListsFragment();
        loadListIntoFragment(loadedListName, loadedList);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame_layout, mainListsFragment)
                .commit();

        super.onCreate(savedInstanceState);
    }

    private void deserializeJsonLists(String listsJson) {
        Type listsType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
        lists = new Gson().fromJson(listsJson, listsType);

        if (lists != null && lists.size() > 0) {
            for (String name : lists.keySet()) {
                drawerItems.put(name, lists.get(name).size());
            }
        } else if (lists == null) {
            lists = new HashMap<String, ArrayList<String>>();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    if (query != null) {
                        mainListsFragment.search(query);
                    }
                    hideKeyboard();
                    searchView.clearFocus();
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (newText != null) {
                        if (newText.equalsIgnoreCase("") && mainListsFragment.getSelectedItem().equals("")) {
                            mainListsFragment.clearSearchFilter();
                        } else {
                            mainListsFragment.search(newText);
                        }
                    }
                    return false;
                }
            });

            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        searchItem.collapseActionView();
                        searchView.setQuery("", false);
                    } else {
                        if (listsDrawerLayout.isDrawerOpen(listsDrawerListView)) {
                            listsDrawerLayout.closeDrawer(listsDrawerListView);
                        }
                    }
                }
            });
        }

        searchView.setQueryHint(getString(R.string.search_hint));
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
                showDeleteListPrompt();
                return true;
            case R.id.action_new_list:
                saveList(mainListsFragment.getListName(), mainListsFragment.getListItems());
                showNewListPrompt();
                return true;
            case R.id.action_rename_list:
                saveList(mainListsFragment.getListName(), mainListsFragment.getListItems());
                showRenameListPrompt();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onFinishAlertDialog() {
        try {
            // Normal delete procedure for saved lists
            deleteList(selectedListName);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Only happens when we try to delete a new list that hasn't been saved yet,
            // Let's just load a new list in this case (kind of like a "clear" function)
            loadListIntoFragment(null, null);
        }
    }

    @Override
    public void onPause() {
        // Make sure dialogs are closed (needed in order to maintain orientation change)
        if (this.alertDialog != null) this.alertDialog.dismiss();
        if (this.inputDialog != null) this.inputDialog.dismiss();

        // Add the current list to the HashMap lists
        saveList(mainListsFragment.getListName(), mainListsFragment.getListItems());
        saveListsToSharedPreferences();
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String input = data.getStringExtra(GenericInputDialog.INTENT_RESULT_KEY).trim();

        if (resultCode == RENAME_LIST_ID) {
            renameList(input);
        } else if (resultCode == NEW_LIST_ID) {
            createNewList(input);
        } else if (resultCode == NEW_CATEGORY_ID) {
            createNewCategory(input);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createNewList(String newListName) {
        if (newListName.trim().equalsIgnoreCase("")) {
            newListName = getDefaultTitle();
        }

        if (!lists.containsKey(newListName)) {
            updateDrawer(newListName, EMPTY_LIST_ITEMS_COUNT);
            lists.put(newListName, new ArrayList<String>());
            selectedListName = newListName;
        }

        loadListIntoFragment(newListName, new ArrayList<String>());
    }

    private void createNewCategory(String newCategoryName) {
        if (newCategoryName.trim().equalsIgnoreCase("")) {
            //TODO newCategoryName = getDefaultCategoryName();
            return;

        }

        if (!lists.containsKey(newCategoryName)) {
            updateDrawer(newCategoryName, LIST_CATEGORY_DEFAULT_COUNT);
            lists.put(newCategoryName, new ArrayList<String>());
            selectedListName = newCategoryName;
        }

        loadListIntoFragment(newCategoryName, new ArrayList<String>());
    }

    private void deleteList(String listName) {
        // If one of these don't contain the list, it means it was never persisted. Return here
        if (!(lists.containsKey(listName) && drawerItems.containsKey(listName))) {
            return;
        }

        lists.remove(listName);

        drawerItems.remove(listName);
        drawerListAdapter.notifyDataSetChanged();
        selectedListName = "";

        loadListIntoFragment(null, null);
    }

    private void renameList(String newListName) {
        if (newListName.equalsIgnoreCase("")) {
            return;
        }

        ArrayList<String> currentListItems = mainListsFragment.getListItems();
        String oldListName = mainListsFragment.getListName();

        if (!lists.containsKey(newListName)) {
            updateDrawer(newListName, currentListItems.size());
            lists.put(newListName, currentListItems);
            deleteList(oldListName);
            selectedListName = newListName;
        }

        loadListIntoFragment(newListName, currentListItems);
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
     * Hide the software keyboard.
     */
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * Save the current list of lists to SharedPreferences.
     */
    public void saveListsToSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor sharedPrefsEditor = getPreferences(MODE_PRIVATE).edit();

        // If the user wants to load the last opened list on startup, save the list's name
        if (sharedPrefs.getBoolean(SettingsActivity.KEY_PREF_STARTUP_OPTION, false)) {
            sharedPrefsEditor.putString(LAST_OPENED_LIST_SP_KEY, mainListsFragment.getListName());
        } else {
            sharedPrefsEditor.putString(LAST_OPENED_LIST_SP_KEY, "");
        }

        Gson gson = new Gson();
        String listsJson = gson.toJson(lists);
        sharedPrefsEditor.putString(LISTS_SP_KEY, listsJson);
        sharedPrefsEditor.commit();
    }

    /**
     * Prompt the user for the name of a list to be created.
     */
    private void showNewListPrompt() {
        // Show dialog for the name of the list, check for duplicates on drawerItems
        showInputDialog(NEW_LIST_ID);
    }

    /**
     *
     */
    private void showNewCategoryPrompt() {
        showInputDialog(NEW_CATEGORY_ID);
    }

    /**
     * Prompt the user for the name of the list to be renamed.
     */
    private void showRenameListPrompt() {
        showInputDialog(RENAME_LIST_ID);
    }

    /**
     * Delete the currently checked list on the Navigation Drawer.
     */
    private void showDeleteListPrompt() {
        showDeleteListConfirmationDialog();
    }

    /**
     * Show the SettingsFragment
     */
    private void showSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * Show the list name input dialog.
     */
    private void showInputDialog(int inputType) {
        FragmentManager fragManager = getFragmentManager();
        inputDialog = new GenericInputDialog();
        inputDialog.setActionIdentifier(inputType);

        if (inputType == NEW_CATEGORY_ID) {
            inputDialog.setTitle(CATEGORY_DIALOG_TITLE);
        } else {
            inputDialog.setTitle(LIST_NAME_DIALOG_TITLE);
        }
        inputDialog.show(fragManager, INPUT_DIALOG_TAG);
    }

    /**
     * Show the Delete List prompt dialog.
     */
    private void showDeleteListConfirmationDialog() {
        FragmentManager fragManager = getFragmentManager();
        alertDialog = new DeleteConfirmationDialog();
        alertDialog.setTitle(CONFIRM_DELETE_DIALOG_TITLE);
        alertDialog.show(fragManager, ALERT_DIALOG_TAG);
    }


    /**
     * Update the Navigation Drawer ListView with the new list listName.
     * @param listName the new list to add to the Navigation Drawer.
     */
    private void updateDrawer(String listName, int listItemsCount) {
        drawerItems.put(listName, listItemsCount);
        drawerListAdapter.notifyDataSetChanged();
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

        if ((listItems.size() > 0) || (lists.containsKey(listName))) {
            lists.put(listName, listItems);
            updateDrawer(listName, listItems.size());
        }
    }

    /**
     * @return the default title for a new list.
     */
    private String getDefaultTitle() {
        return DEFAULT_LIST_NAME + (lists.size() + 1); // Offset index 0
    }

    public void closeSearchView() {
        searchView.setQuery("", false);
        searchView.setIconified(false);
        searchView.clearFocus();
    }


    /* Private Inner Classes from this point onward */

    private class ListsDrawerClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Before loading a new list, make sure the currently loaded one is saved
            saveList(mainListsFragment.getListName(), mainListsFragment.getListItems());

            // Offset position by 1 because of the header (header @ index 0)
            if (--position < 0) {
                showNewListPrompt();
            } else {
                // Load the list specified by position 'position' on the nav drawer
                selectedListName = drawerListAdapter.getKeyAtPosition(position);
                loadListIntoFragment(selectedListName, lists.get(selectedListName));
            }

            // Close the nav drawer
            view.setSelected(true);
            listsDrawerLayout.closeDrawer(listsDrawerListView);
        }
    }
}
