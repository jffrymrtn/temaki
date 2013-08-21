package com.jmartin.temaki.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.jmartin.temaki.R;

/**
 * Author: Jeff Martin, 2013
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
