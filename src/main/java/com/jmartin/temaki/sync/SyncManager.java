package com.jmartin.temaki.sync;

import android.app.Activity;
import android.content.Context;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.jmartin.temaki.model.Constants;
import com.jmartin.temaki.model.TemakiItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by jeff on 2013-09-06.
 */
public class SyncManager {

    private Context context;

    private DbxAccountManager dbxAcctMgr;
    private DbxAccount dbxAccount;
    private DbxDatastore datastore;
    private ArrayList<DbxTable> itemsTbls;

    public SyncManager(Context ctx) {
        context = ctx;
        dbxAcctMgr = DbxAccountManager.getInstance(context, Constants.DB_APP_KEY, Constants.DB_APP_SECRET);
    }

    public void init() {
        if (dbxAcctMgr.hasLinkedAccount()) {
            setupDropboxAccount();
        }
    }

    public void linkDropboxAccount(Activity activity) {
        dbxAcctMgr.startLink(activity, Constants.DBX_LINK_REQUEST_ID);
    }

    public void setupDropboxAccount() {
        dbxAccount = dbxAcctMgr.getLinkedAccount();

        // Open default datastore
        try {
            datastore = DbxDatastore.openDefault(dbxAccount);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public void unlinkDropboxAccount() {
        try {
            datastore.getManager().deleteDatastore(datastore.getId());
        } catch (DbxException e) {
            // TODO handle
        }
        dbxAcctMgr.unlink();
    }

    public void syncDropbox() {
        try {
            datastore.sync();
        } catch (DbxException e) {
            // TODO handle
        }
    }

    public ArrayList<DbxTable> getTables() {
        Set<DbxTable> tables = datastore.getTables();
        itemsTbls = new ArrayList<DbxTable>(tables);
        return itemsTbls;
    }

    /**
     * Read the items from the datastore
     */
    public HashMap<String, ArrayList<TemakiItem>> loadItemsFromDropbox() {
        HashMap<String, ArrayList<TemakiItem>> result = new HashMap<String, ArrayList<TemakiItem>>();

        return result;
    }

    public void createItemRecord(String listName, TemakiItem item) {
        DbxTable table = datastore.getTable(listName);
        table.insert().set(Constants.TABLE_ITEM_TITLE, item.getText())
                      .set(Constants.TABLE_ITEM_ISFINISHED, item.isFinished())
                      .set(Constants.TABLE_ITEM_ISHIGHLIGHTED, item.isHighlighted());
        try {
            datastore.sync();
        } catch (DbxException e) {
            // TODO handle
        }
    }

    public void renameItemRecord(String listName, String oldTitle, String newTitle) {
        DbxTable table = datastore.getTable(listName);
        DbxFields queryParams = new DbxFields().set(Constants.TABLE_ITEM_TITLE, oldTitle);

        try {
            DbxTable.QueryResult queryResult = table.query(queryParams);
            DbxRecord record = queryResult.iterator().next();

            record.set(Constants.TABLE_ITEM_TITLE, newTitle);
            datastore.sync();
        } catch (DbxException e) {
            // TODO handle
        }
    }

    public void toggleItemHighlight(String listName, String itemTitle) {
        DbxTable table = datastore.getTable(listName);
        DbxFields queryParams = new DbxFields().set(Constants.TABLE_ITEM_TITLE, itemTitle);

        try {
            DbxTable.QueryResult queryResult = table.query(queryParams);
            DbxRecord record = queryResult.iterator().next();
            boolean oldHighlight = record.getBoolean(Constants.TABLE_ITEM_ISHIGHLIGHTED);

            record.set(Constants.TABLE_ITEM_ISHIGHLIGHTED, !oldHighlight);
            datastore.sync();
        } catch (DbxException e) {
            // TODO handle
        }
    }

    public void toggleItemFinished(String listName, String itemTitle) {
        DbxTable table = datastore.getTable(listName);
        DbxFields queryParams = new DbxFields().set(Constants.TABLE_ITEM_TITLE, itemTitle);

        try {
            DbxTable.QueryResult queryResult = table.query(queryParams);
            DbxRecord record = queryResult.iterator().next();
            boolean oldFinished = record.getBoolean(Constants.TABLE_ITEM_ISFINISHED);

            record.set(Constants.TABLE_ITEM_ISFINISHED, !oldFinished);
            datastore.sync();
        } catch (DbxException e) {
            // TODO handle
        }
    }

    public void deleteItem(String listName, TemakiItem item) {
        DbxTable table = datastore.getTable(listName);
        DbxFields queryParams = new DbxFields().set(Constants.TABLE_ITEM_TITLE, item.getText());

        try {
            DbxTable.QueryResult queryResult = table.query(queryParams);
            DbxRecord record = queryResult.iterator().next();

            record.deleteRecord();
            datastore.sync();
        } catch (DbxException e) {
            // TODO handle
        }
    }

    public void createNewListTable(String listName) {
        datastore.getTable(listName);
    }

    public void deleteListTable(String listName) {
//        datastore.getTable(listName).
    }

    public void renameListTable(String oldListName, String newListName) {

    }

    public void updateListTable(String listName) {

    }

    public boolean isSyncAvailable() {
        return dbxAcctMgr.hasLinkedAccount();
    }
}
