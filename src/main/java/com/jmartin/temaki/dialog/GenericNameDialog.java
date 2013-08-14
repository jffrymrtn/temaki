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
public class GenericNameDialog extends DialogFragment implements TextView.OnEditorActionListener {

    public interface GenericNameDialogListener {
        void onFinishDialog(String inputValue);
    }

    private EditText promptEditText;
    private String dialogTitle;

    public GenericNameDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.generic_name_dialog_fragment, container);
        promptEditText = (EditText) rootView.findViewById(R.id.prompt_value_edittext);

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

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            finishDialogWithResult();
            return true;
        }
        return false;
    }

    private void finishDialogWithResult() {
        String inputValue = promptEditText.getText().toString();

        GenericNameDialogListener listener = (GenericNameDialogListener) getActivity();
        listener.onFinishDialog(inputValue);
        this.dismiss();
    }

    public void setTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }
}
