package com.jmartin.temaki.settings;

import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Created by jeff on 2013-08-21.
 */
public class SettingsActivity extends Activity {

    public static String KEY_PREF_STARTUP_OPTION = "pref_startup_option";
    public static String KEY_PREF_LIST_ITEMS_CAPITALIZE_OPTION = "pref_list_items_capitalize_option";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
