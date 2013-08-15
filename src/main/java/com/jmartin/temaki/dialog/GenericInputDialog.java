package com.jmartin.temaki.dialog;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jmartin.temaki.MainListsFragment;
import com.jmartin.temaki.R;

/**
 * Author: Jeff Martin, 2013
 */
public class GenericInputDialog extends DialogFragment implements TextView.OnEditorActionListener {

    public interface GenericInputDialogListener {
        void onFinishDialog(String inputValue);
    }

    public static final String INTENT_RESULT_KEY = "Input";
    private EditText promptEditText;
    private String dialogTitle;
    private String optionalExistingValue;

    public GenericInputDialog() {
    }

    public GenericInputDialog(String existingInput) {
        this.optionalExistingValue = existingInput;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.generic_name_dialog_fragment, container);
        promptEditText = (EditText) rootView.findViewById(R.id.prompt_value_edittext);

        if (optionalExistingValue != null && optionalExistingValue.length() > 0) {
            promptEditText.setText(optionalExistingValue);
            promptEditText.setSelection(optionalExistingValue.length());
        }

        Button cancelButton = (Button) rootView.findViewById(R.id.cancel_dialog_button);
        Button okButton = (Button) rootView.findViewById(R.id.ok_dialog_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment frag = getTargetFragment();
                if (frag != null) {
                    frag.onActivityResult(getTargetRequestCode(),
                            MainListsFragment.CANCEL_RESULT_CODE, null);
                }
                dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment frag = getTargetFragment();
                if (frag == null) {
                    finishDialogWithResult();
                } else {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(INTENT_RESULT_KEY, promptEditText.getText().toString());
                    frag.onActivityResult(getTargetRequestCode(),
                            ((MainListsFragment)frag).EDIT_ITEM_ID, resultIntent);
                    dismiss();
                }
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

        GenericInputDialogListener listener = (GenericInputDialogListener) getActivity();
        listener.onFinishDialog(inputValue);
        this.dismiss();
    }

    public void setTitle(String dialogTitle) {
        this.dialogTitle = dialogTitle;
    }
}
