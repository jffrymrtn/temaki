package com.jmartin.temaki.dialog;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jmartin.temaki.MainListsFragment;
import com.jmartin.temaki.R;

/**
 * Created by jeff on 2013-08-20.
 */
public class SettingsAlertDialog extends DialogFragment {

    private String dialogTitle;
    private String dialogText;

    public SettingsAlertDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.settings_alert_dialog_fragment, container);

        TextView dialogTextView = (TextView) rootView.findViewById(R.id.dialog_text);
        dialogTextView.setText(dialogText);

        Button okButton = (Button) rootView.findViewById(R.id.ok_dialog_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        getDialog().setTitle(dialogTitle);
        return rootView;
    }

    public void setTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }

    public void setText(String dialogText) {
        this.dialogText = dialogText;
    }
}
