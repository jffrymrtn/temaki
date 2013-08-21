package com.jmartin.temaki.settings;

import android.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * Created by jeff on 2013-08-21.
 */
public class SettingsActivity extends Activity {

    public static String KEY_PREF_STARTUP_OPTION = "pref_startup_option";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();
    }
}
