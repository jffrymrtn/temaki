package com.jmartin.temaki.settings;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.jmartin.temaki.R;

/**
 * Created by jeff on 2013-08-21.
 */
public class InformationDialogPreference extends DialogPreference {

    public InformationDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.information_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(null);
        setDialogIcon(null);
    }
}
