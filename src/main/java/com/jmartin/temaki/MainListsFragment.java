package com.jmartin.temaki;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jmartin.temaki.adapter.ListItemsAdapter;
import com.jmartin.temaki.dialog.DeleteConfirmationDialog;
import com.jmartin.temaki.dialog.GenericInputDialog;

import java.util.ArrayList;

/**
 * Author: Jeff Martin, 2013
 */
public class MainListsFragment extends Fragment
        implements DeleteConfirmationDialog.GenericAlertDialogListener {

    private final String EDIT_ITEM_DIALOG_TITLE = "Edit List Item:";
    private final String CONFIRM_DELETE_ITEM_DIALOG_TITLE = "Delete this item?";
    private final String ITEM_EXISTS_WARNING = "That item already exists!";
    public static final int CANCEL_RESULT_CODE = 0;
    public static final int DELETE_ITEM_ID = 1;
    public static final int EDIT_ITEM_ID = 2;

    private DeleteConfirmationDialog alertDialog;
    private  GenericInputDialog inputDialog;

    private ListView itemsListView;
    private EditText addItemsEditText;
    private ListItemsAdapter itemsListAdapter;

    private String listName;
    private ArrayList<String> listItems;
    private ActionMode actionMode;

    /* Used for keeping track of selected item. Ideally don't want to do it this way but isSelected
    * is not working in the click listener below.*/
    private TextView selectedItemView = null;
    private String selectedItem = "";

    public MainListsFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment, container, false);

        setActionBarTitle();

        listItems = listItems == null ? new ArrayList<String>() : listItems;

        itemsListView = (ListView) view.findViewById(R.id.mainListView);
        addItemsEditText = (EditText) view.findViewById(R.id.addItemEditText);

        itemsListAdapter = new ListItemsAdapter(getActivity().getApplicationContext(), R.layout.main_list_item, listItems);
        itemsListView.setAdapter(itemsListAdapter);
        itemsListView.setOnItemClickListener(new ListItemClickListener());

        addItemsEditText.setOnClickListener(new EditTextClickListener());
        addItemsEditText.setOnKeyListener(new EditTextKeyListener());
        addItemsEditText.setOnEditorActionListener(new NewItemsEditTextListener());
        addItemsEditText.setOnTouchListener(new AddItemDrawableOnTouchListener(addItemsEditText) {
            @Override
            public boolean onTouchAddItemDrawable(MotionEvent event) {
                addListItem();
                return false;
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        // Make sure dialogs close with this Fragment
        if (this.alertDialog != null) this.alertDialog.dismiss();
        if (this.inputDialog != null) this.inputDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only important if coming from SettingsActivity, make sure any settings styles are applied
        itemsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFinishAlertDialog() {
        listItems.remove(indexOfItem(selectedItem));
        actionMode.finish();
        clearItemSelection();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == DELETE_ITEM_ID) {
            onFinishAlertDialog();
        } else if (resultCode == EDIT_ITEM_ID) {
            renameListItem(data.getStringExtra(GenericInputDialog.INTENT_RESULT_KEY));
        } else if (resultCode == CANCEL_RESULT_CODE) {
            actionMode.finish();
            clearItemSelection();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void renameListItem(String inputValue) {
        int selectedItemPosition = indexOfItem(selectedItem);
        listItems.remove(selectedItemPosition);
        listItems.add(selectedItemPosition, inputValue);
        actionMode.finish();
        clearItemSelection();
    }

    public void clearItemSelection() {
        selectedItem = "";
        itemsListAdapter.setSelectedItemPosition(-1);
        itemsListAdapter.notifyDataSetChanged();

        if (selectedItemView != null) {
            selectedItemView.setBackgroundResource(R.drawable.main_list_item);
            selectedItemView = null;
        }
    }

    /**
     * Custom indexOf for ArrayList<String> listItems so we can do case-insensitive comparisons
     * within indexOf.
     */
    private int indexOfItem(String item) {
        for (int i = 0; i < listItems.size(); i++) {
            if (listItems.get(i).equalsIgnoreCase(item))
                return i;
        }
        return -1;
    }

    /**
     * Load the initial list.
     * @param listName the name of the initial list to load on this Fragment.
     * @param list the list to initially load on this Fragment.
     */
    public void loadList(String listName, ArrayList<String> list) {
        if (this.listItems == null) this.listItems = new ArrayList<String>();

        this.listItems.clear();
        this.listItems.addAll(list);
        this.listName = listName == null ? "" : listName;

        setActionBarTitle();

        if (actionMode != null) {
            actionMode.finish();
        }

        // Notify data set changed if we need to select an item (only happens on orientation change)
        if (itemsListAdapter != null) {
            itemsListAdapter.notifyDataSetChanged();
        }
    }

    private void setActionBarTitle() {
        if ((getActivity() != null) && this.listName != null) {
            getActivity().getActionBar().setTitle(this.listName);
        }
    }

    public String getListName() {
        return listName;
    }

    public ArrayList<String> getListItems() {
        return (ArrayList<String>) listItems.clone();
    }

    /**
     * Show the Edit Item prompt dialog.
     */
    private void showEditItemDialog() {
        FragmentManager fragManager = getFragmentManager();
        inputDialog = new GenericInputDialog(listItems.get(indexOfItem(selectedItem)));

        inputDialog.setTargetFragment(this, EDIT_ITEM_ID);
        inputDialog.setTitle(EDIT_ITEM_DIALOG_TITLE);
        inputDialog.show(fragManager, "generic_name_dialog_fragment");
    }

    /**
     * Show the Delete Item prompt dialog.
     */
    private void showDeleteItemConfirmationDialog() {
        FragmentManager fragManager = getFragmentManager();
        alertDialog = new DeleteConfirmationDialog();

        alertDialog.setTargetFragment(this, DELETE_ITEM_ID);
        alertDialog.setTitle(CONFIRM_DELETE_ITEM_DIALOG_TITLE);
        alertDialog.show(fragManager, "delete_confirmation_dialog_fragment");
    }

    /**
     * Hide the software keyboard.
     */
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public void search(CharSequence query) {
        if (query.length() > 0) {
            itemsListAdapter.getFilter().filter(query);
        }
    }

    public void clearSearchFilter() {
        itemsListAdapter.getFilter().filter("");

        // Workaround to an (apparently) bug in Android's ArrayAdapter... not pretty, I know
        itemsListAdapter = new ListItemsAdapter(getActivity().getApplicationContext(), R.layout.main_list_item, listItems);
        itemsListView.setAdapter(itemsListAdapter);
        itemsListAdapter.notifyDataSetChanged();
    }

    private void addListItem() {
        // Make sure we clear any filters first
        clearSearchFilter();

        if (actionMode != null) {
            actionMode.finish();
        }

        String newItem = addItemsEditText.getText().toString().trim();
        if ((newItem.length() > 0) && indexOfItem(newItem) == -1) {
            listItems.add(newItem);
            itemsListAdapter.notifyDataSetChanged();
            addItemsEditText.setText("");
        } else {
            Toast.makeText(getActivity().getApplicationContext(), ITEM_EXISTS_WARNING, Toast.LENGTH_SHORT).show();
        }
    }

    /* Private Inner Classes from this point onward */

    private class NewItemsEditTextListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE){
                addListItem();
                return true;
            }
            return false;
        }
    }

    private class EditTextKeyListener implements View.OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if ((event.getAction() == KeyEvent.ACTION_UP) && keyCode == KeyEvent.KEYCODE_ENTER) {
                addListItem();
                return true;
            }
            return false;
        }
    }

    private class ListItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (indexOfItem(selectedItem) == position) {
                actionMode.finish();
                clearItemSelection();
            } else {
                clearItemSelection();
                selectedItemView = (TextView) view;
                selectedItem = selectedItemView.getText().toString();

                // Tell the items list adapter that the selected item position changed
                itemsListAdapter.setSelectedItemPosition(position);
                selectedItemView.setBackgroundResource(R.drawable.main_list_item_selected);

                // Show Contextual ActionBar
                if (actionMode == null) {
                    actionMode = getActivity().startActionMode(actionModeCallback);
                }
            }
        }
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.main_list_item_context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.context_menu_edit:
                    showEditItemDialog();
                    return true;
                case R.id.context_menu_delete:
                    showDeleteItemConfirmationDialog();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            clearItemSelection();
            clearSearchFilter();
        }
    };

    private class EditTextClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (actionMode != null) {
                actionMode.finish();
                clearItemSelection();
                clearSearchFilter();

                ((MainDrawerActivity) getActivity()).closeSearchView();
            }
        }
    }

    private abstract class AddItemDrawableOnTouchListener implements View.OnTouchListener {
        Drawable addItemDrawable;
        private final int HITBOX_VALUE = 10;

        public AddItemDrawableOnTouchListener(EditText view) {
            super();
            final Drawable[] drawables = view.getCompoundDrawables();
            if (drawables != null && drawables.length == 4) {
                this.addItemDrawable = drawables[2];
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && addItemDrawable != null) {
                final int x = (int) event.getX();
                final Rect drawableBounds = addItemDrawable.getBounds();

                if ((x <= (v.getRight() - v.getPaddingRight() + HITBOX_VALUE)) &&
                   (x >= (v.getRight() - drawableBounds.width() - HITBOX_VALUE))) {
                    return onTouchAddItemDrawable(event);
                }
            }
            return false;
        }

        public abstract boolean onTouchAddItemDrawable(final MotionEvent event);
    }
}
