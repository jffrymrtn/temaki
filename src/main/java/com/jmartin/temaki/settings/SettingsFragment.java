package com.jmartin.temaki.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.jmartin.temaki.R;
import com.jmartin.temaki.model.Constants;

import java.util.Locale;

/**
 * Author: Jeff Martin, 2013
 */
public class SettingsFragment extends PreferenceFragment {

    private final String TEMAKI_GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id=com.jmartin.temaki";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

//        Preference languagePref = findPreference(Constants.KEY_PREF_LOCALE);
//        String defaultLocale = Locale.getDefault().toString();
//        languagePref.setDefaultValue(defaultLocale);

        Preference ratePref = findPreference(Constants.KEY_PREF_RATE_TEMAKI);
        ratePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(TEMAKI_GOOGLE_PLAY_URL));
                startActivity(intent);

                return false;
            }
        });
    }
}
