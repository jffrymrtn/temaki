package com.jmartin.temaki.dialog;

import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jmartin.temaki.MainListsFragment;
import com.jmartin.temaki.R;

/**
 * Author: Jeff Martin, 2013
 */
public class DeleteConfirmationDialog extends DialogFragment {

    public interface GenericAlertDialogListener {
        void onFinishAlertDialog();
    }

    private String dialogTitle;

    public DeleteConfirmationDialog() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.delete_confirmation_dialog_fragment, container);

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
                    frag.onActivityResult(getTargetRequestCode(),
                                     ((MainListsFragment)frag).DELETE_ITEM_ID, null);
                    dismiss();
                }
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
