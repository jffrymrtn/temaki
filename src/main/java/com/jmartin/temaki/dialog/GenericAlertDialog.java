package com.jmartin.temaki.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jmartin.temaki.R;

/**
 * Author: Jeff Martin, 2013
 */
public class GenericAlertDialog extends DialogFragment {

    public interface GenericAlertDialogListener {
        void onFinishAlertDialog();
    }

    private String dialogTitle;

    public GenericAlertDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.generic_alert_dialog_fragment, container);

        Button cancelButton = (Button) rootView.findViewById(R.id.cancel_dialog_button);
        Button okButton = (Button) rootView.findViewById(R.id.ok_dialog_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDialogWithResult();
            }
        });
        getDialog().setTitle(dialogTitle);
        return rootView;
    }

    private void finishDialogWithResult() {
        GenericAlertDialogListener listener = (GenericAlertDialogListener) getActivity();
        listener.onFinishAlertDialog();
        this.dismiss();
    }

    public void setTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }
}
