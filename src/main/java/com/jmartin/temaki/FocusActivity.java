package com.jmartin.temaki;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.jmartin.temaki.dialog.GenericInputDialog;
import com.jmartin.temaki.model.Constants;
import com.jmartin.temaki.model.TemakiItem;

/**
 * Created by jeff on 2013-09-10.
 */
public class FocusActivity extends Activity {

    TextView focusTextView;
    ImageButton finishedFocusButton;
    TemakiItem focusItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_layout);

        String focusBundledText = getIntent().getStringExtra(Constants.FOCUS_BUNDLE_ID);
        focusItem = new TemakiItem(focusBundledText);

        focusTextView = (TextView) findViewById(R.id.focus_text_view);
        focusTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editFocusText();
            }
        });

        finishedFocusButton = (ImageButton) findViewById(R.id.focus_finished_image_button);
        finishedFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cool animation showing the task is finished and delete it from the list
                focusItem = null;
            }
        });

        if (!focusItem.getText().equalsIgnoreCase("")) {
            focusTextView.setText(focusItem.getText());
        }

        initActionBar();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.focus_anim_slide_out_right, R.anim.focus_anim_slide_in_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void finish() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.FOCUS_TITLE, focusItem.getText());
        setResult(Activity.RESULT_OK, resultIntent);
        super.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.EDIT_FOCUS_ID) {
            String input = data.getStringExtra(Constants.INTENT_RESULT_KEY);
            setFocusText(input);
            focusTextView.setText(input);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        SpannableString abTitle = new SpannableString(getResources().getString(R.string.focus));
        abTitle.setSpan(new TypefaceSpan("sans-serif-light"), 0, abTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getActionBar().setTitle(abTitle);
    }

    private void editFocusText() {
        FragmentManager fragManager = getFragmentManager();
        GenericInputDialog inputDialog = new GenericInputDialog(focusItem.getText());

        inputDialog.setActionIdentifier(Constants.EDIT_FOCUS_ID);
        inputDialog.setTitle(getResources().getString(R.string.edit_focus_dialog_title));
        inputDialog.show(fragManager, "generic_name_dialog_fragment");
    }

    public String getFocusText() {
        if (focusItem == null) {
            return "";
        }
        return focusItem.getText();
    }

    public void setFocusText(String newFocus) {
        this.focusItem.setText(newFocus);
    }
}
