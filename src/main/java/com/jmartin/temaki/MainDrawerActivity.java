package com.jmartin.temaki;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jmartin.temaki.adapter.DrawerListAdapter;
import com.jmartin.temaki.dialog.DeleteConfirmationDialog;
import com.jmartin.temaki.dialog.GenericInputDialog;
import com.jmartin.temaki.model.Constants;
import com.jmartin.temaki.model.TemakiItem;
import com.jmartin.temaki.settings.SettingsActivity;
import com.jmartin.temaki.sync.SyncManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Author: Jeff Martin, 2013
 */
public class MainDrawerActivity extends FragmentActivity
        implements DeleteConfirmationDialog.GenericAlertDialogListener {

    private GenericInputDialog inputDialog;
    private DeleteConfirmationDialog alertDialog;

    private DrawerLayout listsDrawerLayout;
    private ListView listsDrawerListView;
    private ActionBarDrawerToggle listsDrawerToggle;
    private LinkedHashMap<String, Integer> drawerItems;
    private DrawerListAdapter drawerListAdapter;
    private HashMap<String, ArrayList<TemakiItem>> lists;

    FocusActivity focusActivity;
    private MainListsFragment mainListsFragment;
    private SearchView searchView;

    private SyncManager syncManager = null;

    /* Used for keeping track of selected item. Ideally don't want to do it this way but isSelected
    * is not working in the click listener below.*/
    private String selectedListName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.main_drawer_layout);

        // Optimize overdraw on window background
        getWindow().setBackgroundDrawable(null);

        drawerItems = new LinkedHashMap<String, Integer>();
        ArrayList<TemakiItem> loadedList = null;

        // Set the locale in case the user changed it
        setLocale();

        // If Dropbox Sync is enabled
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.KEY_PREF_DROPBOX_SYNC, false)) {
            syncManager = new SyncManager(getApplicationContext());
            syncManager.init();
            lists = syncManager.loadItemsFromDropbox();
        } else {
            // Load metadata and lastLoadedList
            String listsJson = initListsJson(savedInstanceState);

            // Initialize lists variable
            deserializeJsonLists(listsJson);
        }

        String loadedListName = initLastLoadedList(savedInstanceState);

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
        View focusHeaderView = getLayoutInflater().inflate(R.layout.drawer_focus_header, null);
        listsDrawerListView.addHeaderView(drawerListViewHeaderView);
        listsDrawerListView.addHeaderView(focusHeaderView);

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
                if (mainListsFragment.isVisible()) {
                    setActionBarCustomTitle(mainListsFragment.getCapitalizedListName());
                } else {
                    setActionBarCustomTitle(Constants.FOCUS_TITLE);
                }

                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View view) {
                setActionBarCustomTitle(getTitle().toString());

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

        // Set up ActionBar
        initActionBar();

        // Set up Preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        focusActivity = new FocusActivity();

        // Load the main fragment with an empty list
        mainListsFragment = new MainListsFragment();
        loadListIntoFragment(loadedListName, loadedList);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame_layout, mainListsFragment)
                .commit();

        // !! Workaround to windowContentOverlay bug in Android API Level 18, REMOVE when Google fixes it
        windowContentOverlayWorkaround();

        super.onCreate(savedInstanceState);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        setActionBarCustomTitle(getTitle().toString());
    }

    private String initListsJson(Bundle savedInstanceState) {
        String listsJson = "";
        if (savedInstanceState != null) {
            // load from savedInstanceState
            listsJson = savedInstanceState.getString(Constants.LISTS_SP_KEY, "");
        } else {
            // Load from SharedPreferences
            SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);
            listsJson = sharedPrefs.getString(Constants.LISTS_SP_KEY, "");
        }
        return listsJson;
    }

    private String initLastLoadedList(Bundle savedInstanceState) {
        String loadedListName = "";
        if (savedInstanceState == null) {
            // Load from SharedPreferences
            SharedPreferences sharedPrefs = getPreferences(MODE_PRIVATE);

            // Load the last loaded list if needed
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.KEY_PREF_STARTUP_OPTION, false)) {
                loadedListName = sharedPrefs.getString(Constants.LAST_OPENED_LIST_SP_KEY, "");
            }
        } else {
            // load from savedInstanceState
            loadedListName = savedInstanceState.getString(Constants.LIST_NAME_BUNDLE_KEY, "");
        }
        return loadedListName;
    }

    /**
     * Sets the custom locale of the application if the user changed it in Settings.
     */
    private void setLocale() {
        Configuration updatedConfig = getBaseContext().getResources().getConfiguration();
        String defaultLocale = Locale.getDefault().toString();
        String stringLocale = PreferenceManager.getDefaultSharedPreferences(this).getString(Constants.KEY_PREF_LOCALE, defaultLocale);

        if (!stringLocale.equals("")) {
            Locale locale = new Locale(stringLocale);

            Locale.setDefault(locale);
            updatedConfig.locale = locale;
            getBaseContext().getResources().updateConfiguration(updatedConfig, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    /**
     * Sets the ActionBar title to the parameter 'title'.
     */
    private void setActionBarCustomTitle(String title) {
        SpannableString abTitle = new SpannableString(title);
        abTitle.setSpan(new TypefaceSpan("sans-serif-light"), 0, abTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActionBar().setTitle(abTitle);
    }

    /**
     * !IMPORTANT! Workaround to a bug in Android API Level 18. Remove when fixed
     */
    private void windowContentOverlayWorkaround() {
        View contentView = findViewById(android.R.id.content);

        if (contentView instanceof FrameLayout) {
            TypedValue typedValue = new TypedValue();

            if (getTheme().resolveAttribute(android.R.attr.windowContentOverlay, typedValue, true)) {
                if (typedValue.resourceId != 0) {
                    ((FrameLayout) contentView).setForeground(getResources().getDrawable(typedValue.resourceId));
                }
            }
        }
    }

    /**
     * Deserializes the JSON string listsJson into its respective Collection<?> type.
     */
    private void deserializeJsonLists(String listsJson) {
        Type listsType = new TypeToken<HashMap<String, ArrayList<TemakiItem>>>() {}.getType();
        try {
            lists = new Gson().fromJson(listsJson, listsType);
        } catch (Exception e) {
            // Compatibility code, we need this for users who are coming from older versions of the app
            listsType = new TypeToken<HashMap<String, ArrayList<String>>>() {}.getType();
            HashMap<String, ArrayList<String>> compatLists = new Gson().fromJson(listsJson, listsType);

            // Convert these to the new type
            lists = new HashMap<String, ArrayList<TemakiItem>>();
            for (String key : compatLists.keySet()) {
                ArrayList<TemakiItem> newListType = new ArrayList<TemakiItem>();
                for (String item : compatLists.get(key)) {
                    newListType.add(new TemakiItem(item));
                }
                lists.put(key, newListType);
            }
        }

        if (lists != null && lists.size() > 0) {
            for (String name : lists.keySet()) {
                drawerItems.put(name, lists.get(name).size());
            }
        } else if (lists == null) {
            lists = new HashMap<String, ArrayList<TemakiItem>>();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem renameListItem = menu.findItem(R.id.action_rename_list);
        MenuItem deleteListItem = menu.findItem(R.id.action_delete_list);

        searchView = (SearchView) searchItem.getActionView();

        // Configure SearchView TextView font stuff
        int textViewId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView searchViewTextView = (TextView) searchView.findViewById(textViewId);
        searchViewTextView.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));

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
                        if (mainListsFragment.isVisible()) {
                            if (newText.equalsIgnoreCase("") && mainListsFragment.getSelectedItem().equals("")) {
                                mainListsFragment.clearSearchFilter();
                            } else {
                                mainListsFragment.search(newText);
                            }
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
        outState.putString(Constants.LISTS_SP_KEY, jsonLists);

        outState.putString(Constants.LIST_NAME_BUNDLE_KEY, mainListsFragment.getListName());
        outState.putString(Constants.LIST_ITEMS_BUNDLE_KEY, gson.toJson(mainListsFragment.getListItems()));
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

        // Get the JSON string we need to save
        Gson gson = new Gson();
        String listsJson = gson.toJson(lists);

        saveListsToSharedPreferences(listsJson);

        if (syncManager != null && syncManager.isSyncAvailable()) {
            syncManager.syncDropbox();
        }
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.DBX_LINK_REQUEST_ID) {
            if (resultCode == Activity.RESULT_OK) {
                syncManager.setupDropboxAccount();
                loadDatastoreTables();
            } else {
                Log.d("Dropbox Link", "Dropbox link failed");
            }
        } else {
            String input = data.getStringExtra(Constants.INTENT_RESULT_KEY).trim();
            if (resultCode == Constants.RENAME_LIST_ID) {
                renameList(input);
            } else if (resultCode == Constants.NEW_LIST_ID) {
                createNewList(input);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * This method gets called when the app is linked with Dropbox, and never again.
     */
    private void loadDatastoreTables() {
        // If Dbx Datastore has records, load them here as new lists or insert items to existing
        // lists if applicable
        ArrayList<DbxTable> dbxTables = syncManager.getTables();

        // Note: table = list, record = item
        for (DbxTable tbl : dbxTables) {
            String listName = tbl.getId();
            try {
                DbxTable.QueryResult records = tbl.query();
                Iterator<DbxRecord> recordIterator = records.iterator();
                ArrayList<TemakiItem> lst = new ArrayList<TemakiItem>();

                while (recordIterator.hasNext()) {
                    DbxRecord record = recordIterator.next();
                    String item = record.getString("title");
                    boolean isFinished = record.getBoolean("isFinished");
                    boolean isHighlighted = record.getBoolean("isHighlighted");


                    if (lists.containsKey(listName)) {
                        lst = lists.get(listName);
                    }

                    if (!isItemInList(lst, item)) {
                        TemakiItem temakiItem = new TemakiItem(item);

                        if (isFinished) temakiItem.toggleFinished();
                        if (isHighlighted) temakiItem.toggleHighlighted();

                        lst.add(0, temakiItem);
                    }
                }
                lists.put(listName, lst);
                updateDrawer(listName, lst.size());
            } catch (DbxException e) {
                // TODO handle
            }
        }

    }

    /**
     * Return whether or not the item 'item' exists in ArrayList lst.
     */
    private boolean isItemInList(ArrayList<TemakiItem> lst, String item) {
        for (TemakiItem itm : lst) {
            if (itm.getText().equalsIgnoreCase(item)) {
                return true;
            }
        }
        return false;
    }

    private void createNewList(String newListName) {
        if (newListName.trim().equalsIgnoreCase("")) {
            newListName = getDefaultTitle();
        }

        if (!lists.containsKey(newListName)) {
            updateDrawer(newListName, Constants.EMPTY_LIST_ITEMS_COUNT);
            lists.put(newListName, new ArrayList<TemakiItem>());
            selectedListName = newListName;
        }

        if (syncManager != null && syncManager.isSyncAvailable()) {
            syncManager.createNewListTable(newListName);
        }

        loadListIntoFragment(newListName, lists.get(newListName));
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

        if (syncManager != null && syncManager.isSyncAvailable()) {
            syncManager.deleteListTable(listName);
        }

        loadListIntoFragment(null, null);
    }

    private void renameList(String newListName) {
        if (newListName.equalsIgnoreCase("")) {
            return;
        }

        ArrayList<TemakiItem> currentListItems = mainListsFragment.getListItems();
        String oldListName = mainListsFragment.getListName();

        if (!lists.containsKey(newListName)) {
            updateDrawer(newListName, currentListItems.size());
            lists.put(newListName, currentListItems);
            deleteList(oldListName);
            selectedListName = newListName;
        }

        if (syncManager != null && syncManager.isSyncAvailable()) {
            syncManager.renameListTable(oldListName, newListName);
        }

        loadListIntoFragment(newListName, currentListItems);
    }

    /**
     * Load the list 'list' with name 'listName'.
     * @param listName the name of the list to load.
     * @param list the list to load.
     */
    public void loadListIntoFragment(String listName, ArrayList<TemakiItem> list) {
        if (!getActionBar().isShowing()) {
            getActionBar().show();
        }

        if (listName == null || list == null) {
            listName = getDefaultTitle();
            list = new ArrayList<TemakiItem>();
        }

        setActionBarCustomTitle(listName);
        mainListsFragment.loadList(listName, list);

        if (syncManager != null && syncManager.isSyncAvailable()) {
            mainListsFragment.loadSyncManager(syncManager);
        }

        if (!mainListsFragment.isVisible()) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame_layout, mainListsFragment)
                    .commit();
        }
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
    public void saveListsToSharedPreferences(String listsJson) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor sharedPrefsEditor = getPreferences(MODE_PRIVATE).edit();

        // If the user wants to load the last opened list on startup, save the list's name
        if (sharedPrefs.getBoolean(Constants.KEY_PREF_STARTUP_OPTION, false)) {
            sharedPrefsEditor.putString(Constants.LAST_OPENED_LIST_SP_KEY, mainListsFragment.getListName());
        } else {
            sharedPrefsEditor.putString(Constants.LAST_OPENED_LIST_SP_KEY, "");
        }

        sharedPrefsEditor.putString(Constants.LISTS_SP_KEY, listsJson);
        sharedPrefsEditor.commit();
    }

    /**
     * Prompt the user for the name of a list to be created.
     */
    private void showNewListPrompt() {
        // Show dialog for the name of the list, check for duplicates on drawerItems
        showInputDialog(Constants.NEW_LIST_ID);
    }

    /**
     * Prompt the user for the name of the list to be renamed.
     */
    private void showRenameListPrompt() {
        showInputDialog(Constants.RENAME_LIST_ID);
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

    @Override
    protected void onResume() {
        // Set the locale in case the user changed it
        setLocale();
        
        // Check if Dropbox sync was enabled from Preferences
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Constants.KEY_PREF_DROPBOX_SYNC, false)) {
            initDropboxSync();
        } else {
            if (syncManager != null) {
                syncManager.unlinkDropboxAccount();
            }
            syncManager = null;
        }

        super.onResume();
    }

    private void initDropboxSync() {
        if (syncManager == null) {
            syncManager = new SyncManager(getApplicationContext());
            syncManager.linkDropboxAccount(this);
        }
    }

    /**
     * Show the list name input dialog.
     */
    private void showInputDialog(int inputType) {
        FragmentManager fragManager = getFragmentManager();
        inputDialog = new GenericInputDialog();
        inputDialog.setActionIdentifier(inputType);

        inputDialog.setTitle(getResources().getString(R.string.list_name_dialog_title));
        inputDialog.show(fragManager, Constants.INPUT_DIALOG_TAG);
    }

    /**
     * Show the Delete List prompt dialog.
     */
    private void showDeleteListConfirmationDialog() {
        FragmentManager fragManager = getFragmentManager();
        alertDialog = new DeleteConfirmationDialog();
        alertDialog.setTitle(getResources().getString(R.string.list_delete_confirm_title));
        alertDialog.show(fragManager, Constants.ALERT_DIALOG_TAG);
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
    public void saveList(String listName, ArrayList<TemakiItem> listItems) {
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
        return Constants.DEFAULT_LIST_NAME + (lists.size() + 1); // Offset index 0
    }

    public void closeSearchView() {
        searchView.setQuery("", false);
        searchView.setIconified(false);
        searchView.clearFocus();
    }

    private void showFocusActivity() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        String focusText = sp.getString(Constants.FOCUS_SP_KEY, "");

        Intent focusIntent = new Intent(this, FocusActivity.class);
        focusIntent.putExtra(Constants.FOCUS_BUNDLE_ID, focusText);
        focusIntent.putExtra(Constants.SP_NAME_BUNDLE_ID, getLocalClassName());

        startActivity(focusIntent);
        overridePendingTransition(R.anim.focus_anim_slide_in_left, R.anim.focus_anim_slide_out_left);
    }

    /* Private Inner Classes from this point onward */
    private class ListsDrawerClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Before loading a new list, make sure the currently loaded one is saved
            saveList(mainListsFragment.getListName(), mainListsFragment.getListItems());

            // Offset position by 2 because of the headers (header @ index 0 and 1)
            position = position - 2;
            if (position == -2) {
                showNewListPrompt();
            } else if (position == -1) {
                showFocusActivity();
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
