package com.jmartin.temaki.settings;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.jmartin.temaki.R;
import com.jmartin.temaki.dialog.DeleteConfirmationDialog;
import com.jmartin.temaki.dialog.SettingsAlertDialog;

/**
 * Author: Jeff Martin, 2013
 */
public class SettingsActivity extends Activity {

    private final String ABOUT_DIALOG_FLAG = "about";
    private final String ATTRIBUTIONS_DIALOG_FLAG = "attributions";

    private final String ABOUT_DIALOG_TITLE = "About Temaki";
    private final String ATTRIBUTIONS_DIALOG_TITLE = "Attributions";

    private final String ALERT_DIALOG_TAG = "delete_confirmation_dialog_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        // Buttons
        Button attributionsButton = (Button) findViewById(R.id.attributions_button);
        attributionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        Button aboutButton = (Button) findViewById(R.id.about_button);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSettingsAlertDialog(ABOUT_DIALOG_FLAG);
            }
        });

        // Spinners
        Spinner listStyleSpinner = (Spinner) findViewById(R.id.list_style_spinner);
        ArrayAdapter<CharSequence> listStyleSpinnerAdapter =
                ArrayAdapter.createFromResource(this, R.array.list_style_array, android.R.layout.simple_spinner_item);

        listStyleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listStyleSpinner.setAdapter(listStyleSpinnerAdapter);

        Spinner colorSchemeSpinner = (Spinner) findViewById(R.id.color_scheme_spinner);
        ArrayAdapter<CharSequence> colorSchemeSpinnerAdapter =
                ArrayAdapter.createFromResource(this, R.array.color_schemes_array, android.R.layout.simple_spinner_dropdown_item);

        colorSchemeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSchemeSpinner.setAdapter(colorSchemeSpinnerAdapter);

        // ActionBar
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void showSettingsAlertDialog(String flag) {
        FragmentManager fragManager = getFragmentManager();
        SettingsAlertDialog alertDialog = new SettingsAlertDialog();

        if (flag.equalsIgnoreCase(ABOUT_DIALOG_FLAG)) {
            alertDialog.setTitle(ABOUT_DIALOG_TITLE);
            alertDialog.setText(getString(R.string.about_string));
        } else if (flag.equalsIgnoreCase(ATTRIBUTIONS_DIALOG_FLAG)) {
            alertDialog.setTitle(ATTRIBUTIONS_DIALOG_TITLE);
            alertDialog.setText(getString(R.string.attribution_string));
        }

        alertDialog.show(fragManager, ALERT_DIALOG_TAG);
    }
}
