package com.jmartin.temaki;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jmartin.temaki.adapter.ListItemsAdapter;
import com.jmartin.temaki.dialog.DeleteConfirmationDialog;
import com.jmartin.temaki.dialog.GenericInputDialog;
import com.jmartin.temaki.model.Constants;
import com.jmartin.temaki.model.TemakiItem;

import java.util.ArrayList;

/**
 * Author: Jeff Martin, 2013
 */
public class MainListsFragment extends Fragment
        implements DeleteConfirmationDialog.GenericAlertDialogListener {

    private DeleteConfirmationDialog alertDialog;
    private  GenericInputDialog inputDialog;

    private ListView itemsListView;
    private EditText addItemsEditText;
    private ListItemsAdapter itemsListAdapter;

    private String listName;
    private ArrayList<TemakiItem> listItems;
    private ActionMode actionMode;

    private Toast toast;

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

        listItems = listItems == null ? new ArrayList<TemakiItem>() : listItems;

        itemsListView = (ListView) view.findViewById(R.id.main_list_view);

        addItemsEditText = (EditText) view.findViewById(R.id.add_item_edit_text);

        itemsListAdapter = new ListItemsAdapter(getActivity().getApplicationContext(), listItems);
        itemsListView.setAdapter(itemsListAdapter);
        itemsListView.setOnItemClickListener(new ListItemClickListener());

        addItemsEditText.setOnClickListener(new EditTextClickListener());
        addItemsEditText.setOnKeyListener(new EditTextKeyListener());
        addItemsEditText.setOnEditorActionListener(new NewItemsEditTextListener());

        ImageButton addItemImageButton = (ImageButton) view.findViewById(R.id.add_item_image_button);
        addItemImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addListItem();
            }
        });

        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
        if (resultCode == Constants.DELETE_ITEM_ID) {
            onFinishAlertDialog();
        } else if (resultCode == Constants.EDIT_ITEM_ID) {
            renameListItem(data.getStringExtra(Constants.INTENT_RESULT_KEY));
        } else if (resultCode == Constants.CANCEL_RESULT_CODE) {
            actionMode.finish();
            clearItemSelection();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void renameListItem(String inputValue) {
        int selectedItemPosition = indexOfItem(selectedItem);
        listItems.get(selectedItemPosition).setText(inputValue);
        actionMode.finish();
        clearItemSelection();
    }

    private void toggleItemHighlight() {
        int selectedItemPosition = indexOfItem(selectedItem);
        listItems.get(selectedItemPosition).toggleHighlighted();
        actionMode.finish();
        clearItemSelection();
    }

    private void toggleItemFinished() {
        int selectedItemPosition = indexOfItem(selectedItem);
        listItems.get(selectedItemPosition).toggleFinished();
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
            if (listItems.get(i).getText().equalsIgnoreCase(item))
                return i;
        }
        return -1;
    }

    /**
     * Load the initial list.
     * @param listName the name of the initial list to load on this Fragment.
     * @param list the list to initially load on this Fragment.
     */
    public void loadList(String listName, ArrayList<TemakiItem> list) {
        if (this.listItems == null) this.listItems = new ArrayList<TemakiItem>();

        this.listItems.clear();
        this.listItems.addAll(list);
        this.listName = listName == null ? "" : listName;

        if (actionMode != null) {
            actionMode.finish();
        }

        // Notify data set changed if we need to select an item (only happens on orientation change)
        if (itemsListAdapter != null) {
            itemsListAdapter.notifyDataSetChanged();
        }
    }

    public String getCapitalizedListName() {
        return (this.listName.substring(0, 1).toUpperCase() + this.listName.substring(1).toLowerCase());
    }

    public String getListName() {
        return listName;
    }

    public ArrayList<TemakiItem> getListItems() {
        return (ArrayList<TemakiItem>) listItems.clone();
    }

    public String getSelectedItem() {
        return selectedItem;
    }

    /**
     * Show the Edit TemakiListItem prompt dialog.
     */
    private void showEditItemDialog() {
        FragmentManager fragManager = getFragmentManager();
        inputDialog = new GenericInputDialog(listItems.get(indexOfItem(selectedItem)).getText());

        inputDialog.setTargetFragment(this, Constants.EDIT_ITEM_ID);
        inputDialog.setTitle(getActivity().getApplicationContext().getResources().getString(R.string.edit_item_dialog_title));
        inputDialog.show(fragManager, "generic_name_dialog_fragment");
    }

    /**
     * Show the Delete TemakiListItem prompt dialog.
     */
    private void showDeleteItemConfirmationDialog() {
        FragmentManager fragManager = getFragmentManager();
        alertDialog = new DeleteConfirmationDialog();

        alertDialog.setTargetFragment(this, Constants.DELETE_ITEM_ID);
        alertDialog.setTitle(getActivity().getApplicationContext().getResources().getString(R.string.item_delete_confirm_title));
        alertDialog.show(fragManager, "delete_confirmation_dialog_fragment");
    }

    public void search(CharSequence query) {
        if (query.length() > 0) {
            itemsListAdapter.getFilter().filter(query);
        }
    }

    public void clearSearchFilter() {
        itemsListAdapter.getFilter().filter("");

        // Workaround to an (apparently) bug in Android's ArrayAdapter... not pretty, I know
        itemsListAdapter = new ListItemsAdapter(getActivity().getApplicationContext(), listItems);
        itemsListView.setAdapter(itemsListAdapter);
        itemsListAdapter.notifyDataSetChanged();
    }

    private void addListItem() {
        // Make sure we clear any filters first
        clearSearchFilter();

        if (actionMode != null) {
            actionMode.finish();
        }

        TemakiItem newItem = new TemakiItem(addItemsEditText.getText().toString().trim());
        if ((newItem.getText().length() > 0) && indexOfItem(newItem.getText()) == -1) {
            listItems.add(newItem);
            itemsListAdapter.notifyDataSetChanged();
            addItemsEditText.setText("");
        } else if ((indexOfItem(newItem.getText()) != -1) && (newItem.getText().length() > 0)) {
            Context context = getActivity().getApplicationContext();
            if (toast == null) {
                toast = Toast.makeText(context, context.getResources().getString(R.string.item_exists_toast), Toast.LENGTH_SHORT);
                toast.show();
            }
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
            if (event.getAction() == KeyEvent.ACTION_UP) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    addListItem();
                    return true;
                }
            }
            return false;
        }
    }

    private class ListItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (selectedItem.equalsIgnoreCase(((TextView) view).getText().toString())) {
                clearItemSelection();
                clearSearchFilter();
                actionMode.finish();
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
                case R.id.context_menu_finished:
                    toggleItemFinished();
                    return true;
                case R.id.context_menu_highlight:
                    toggleItemHighlight();
                    return true;
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
}
