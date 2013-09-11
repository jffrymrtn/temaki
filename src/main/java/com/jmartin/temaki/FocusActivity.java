package com.jmartin.temaki;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jmartin.temaki.dialog.GenericInputDialog;
import com.jmartin.temaki.model.Constants;
import com.jmartin.temaki.model.TemakiItem;

/**
 * Created by jeff on 2013-09-10.
 */
public class FocusActivity extends Activity {

    private TextView focusTextView;
    private ImageButton finishedFocusButton;
    private TemakiItem focusItem;

    private String spName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.focus_layout);

        String focusBundledText = getIntent().getStringExtra(Constants.FOCUS_BUNDLE_ID);
        spName = getIntent().getStringExtra(Constants.SP_NAME_BUNDLE_ID);

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
                finishFocus();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.EDIT_FOCUS_ID) {
            String input = data.getStringExtra(Constants.INTENT_RESULT_KEY);
            setFocusText(input);
            focusTextView.setText(input);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        saveFocus();
        super.onPause();
    }

    /**
     * Save the user's Focus list in a separate SharedPreferences than other lists.
     */
    private void saveFocus() {
        SharedPreferences.Editor sharedPrefsEditor = getSharedPreferences(spName, MODE_PRIVATE).edit();
        sharedPrefsEditor.putString(Constants.FOCUS_SP_KEY, focusItem.getText())
                         .commit();
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

    private void finishFocus() {
        focusItem = new TemakiItem("");
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.focus_text_disappear_anim);
        fadeOutAnimation.setAnimationListener(finishFocusAnimationListener);

        focusTextView.startAnimation(fadeOutAnimation);
    }

    private Animation.AnimationListener finishFocusAnimationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            Animation fadeInAnimation = AnimationUtils.loadAnimation(FocusActivity.this, R.anim.focus_text_fade_in_anim);
            focusTextView.setText(getResources().getString(R.string.focus_finished));
            focusTextView.startAnimation(fadeInAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    };
}
