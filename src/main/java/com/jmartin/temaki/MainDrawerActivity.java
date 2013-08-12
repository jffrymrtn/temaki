package com.jmartin.temaki;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Author Jeff Martin, 2013
 */
public class MainDrawerActivity extends Activity {

    private DrawerLayout listsDrawerLayout;
    private ListView listsDrawerListView;
    private ActionBarDrawerToggle listsDrawerToggle;
    private String[] drawerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_drawer_layout);

        drawerItems = new String[3];
        drawerItems[0] = "a";
        drawerItems[1] = "b";
        drawerItems[2] = "c";

        // Set the Navigation Drawer up
        listsDrawerLayout = (DrawerLayout) findViewById(R.id.lists_drawer_layout);
        listsDrawerListView = (ListView) findViewById(R.id.lists_drawer);

        // Set the drawer ListView Header
        View drawerListViewHeaderView = getLayoutInflater().inflate(R.layout.drawer_header, null);
        listsDrawerListView.addHeaderView(drawerListViewHeaderView);

        // Set drawer ListView adapter
        listsDrawerListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerItems));
        listsDrawerListView.setOnItemClickListener(new ListsDrawerClickListener());

        // Set up the ActionBar Drawer Toggle
        listsDrawerToggle = new ActionBarDrawerToggle(this,
                listsDrawerLayout,
                R.drawable.ic_drawer, R.string.open_drawer, R.string.close_drawer) {
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

        // Load the main fragment
        Fragment mainListsFragment = new MainListsFragment();
        // Bundle args = new Bundle();
        // args.putStringArray();
        // mainListsFragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame_layout, mainListsFragment)
                .commit();

        if (savedInstanceState == null) {

        }
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

    private class ListsDrawerClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Offset position by -1 because of header
            if (position-- < 0) {
                return;
            }

            // Load the list specified by position 'position' on the nav drawer

            setTitle(drawerItems[position]);

            // Close the nav drawer
            listsDrawerListView.setItemChecked(position, true);
            listsDrawerLayout.closeDrawer(listsDrawerListView);
        }
    }
}
