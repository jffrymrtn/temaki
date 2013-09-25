package com.jmartin.temaki.sync;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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
import java.util.Iterator;
import java.util.Set;

/**
 * Created by jeff on 2013-09-06.
 */
public class SyncManager {

    private Context context;

    private DbxAccountManager dbxAcctMgr;
    private DbxAccount dbxAccount;
    private DbxDatastore datastore;
    private ArrayList<DbxTable> itemsTables;

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
        openDatastore();
    }

    public void openDatastore() {
        try {
            datastore = DbxDatastore.openDefault(dbxAccount);
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public void closeDatastore() {
        datastore.close();
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
        if (datastore != null) {
            Set<DbxTable> tables = datastore.getTables();
            itemsTables = new ArrayList<DbxTable>(tables);
        }
        return itemsTables;
    }

    public void createItem(String listName, TemakiItem item) {
        DbxTable table = datastore.getTable(getDbFriendlyId(listName));
        table.insert().set(Constants.TABLE_ITEM_TITLE, item.getText())
                      .set(Constants.TABLE_ITEM_ISFINISHED, item.isFinished())
                      .set(Constants.TABLE_ITEM_ISHIGHLIGHTED, item.isHighlighted());
        syncDropbox();
    }

    public void createFocus(String listName, String focus) {
        DbxTable table = datastore.getTable(getDbFriendlyId(listName));

        try {
            table.getOrInsert(Constants.DB_FOCUS_TABLE_NAME).set(Constants.TABLE_ITEM_TITLE, focus);
        } catch (DbxException e) {
            e.printStackTrace();
        }

        syncDropbox();
    }

    public void renameItem(String listName, String oldTitle, String newTitle) {
        DbxTable table = datastore.getTable(getDbFriendlyId(listName));
        DbxFields queryParams = new DbxFields().set(Constants.TABLE_ITEM_TITLE, oldTitle);

        try {
            DbxTable.QueryResult queryResult = table.query(queryParams);
            DbxRecord record = queryResult.iterator().next();

            record.set(Constants.TABLE_ITEM_TITLE, newTitle);
        } catch (DbxException e) {
            // TODO handle
        }

        syncDropbox();
    }

    public void toggleItemHighlight(String listName, String itemTitle) {
        DbxTable table = datastore.getTable(getDbFriendlyId(listName));
        DbxFields queryParams = new DbxFields().set(Constants.TABLE_ITEM_TITLE, itemTitle);

        try {
            DbxTable.QueryResult queryResult = table.query(queryParams);
            DbxRecord record = queryResult.iterator().next();
            boolean oldHighlight = record.getBoolean(Constants.TABLE_ITEM_ISHIGHLIGHTED);

            record.set(Constants.TABLE_ITEM_ISHIGHLIGHTED, !oldHighlight);
        } catch (DbxException e) {
            // TODO handle
        }

        syncDropbox();
    }

    private String getDbFriendlyId(String listName) {
        return listName.replace(" ", "_-_");
    }

    public void toggleItemFinished(String listName, String itemTitle) {
        DbxTable table = datastore.getTable(getDbFriendlyId(listName));
        DbxFields queryParams = new DbxFields().set(Constants.TABLE_ITEM_TITLE, itemTitle);

        try {
            DbxTable.QueryResult queryResult = table.query(queryParams);
            DbxRecord record = queryResult.iterator().next();
            boolean oldFinished = record.getBoolean(Constants.TABLE_ITEM_ISFINISHED);

            record.set(Constants.TABLE_ITEM_ISFINISHED, !oldFinished);
        } catch (DbxException e) {
            // TODO handle
        }

        syncDropbox();
    }

    public void deleteItem(String listName, TemakiItem item) {
        DbxTable table = datastore.getTable(getDbFriendlyId(listName));
        DbxFields queryParams = new DbxFields().set(Constants.TABLE_ITEM_TITLE, item.getText());

        try {
            DbxTable.QueryResult queryResult = table.query(queryParams);
            DbxRecord record = queryResult.iterator().next();

            record.deleteRecord();
        } catch (DbxException e) {
            // TODO handle
        }

        syncDropbox();
    }

    public void createNewList(String listName) {
        datastore.getTable(getDbFriendlyId(listName));
    }

    public void deleteList(String listName) {
        try {
            Iterator iter = datastore.getTable(getDbFriendlyId(listName)).query().iterator();
            while (iter.hasNext()) {
                DbxRecord record = (DbxRecord) iter.next();
                record.deleteRecord();
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public void renameList(String oldListName, String newListName) {

    }

    public boolean isSyncAvailable() {
        return dbxAcctMgr.hasLinkedAccount();
    }

    public void registerListener() {
        datastore.addSyncStatusListener(datastoreSyncStatusListener);
    }

    public void deregisterListener() {
        datastore.removeSyncStatusListener(datastoreSyncStatusListener);
    }

    private DbxDatastore.SyncStatusListener datastoreSyncStatusListener = new DbxDatastore.SyncStatusListener() {
        @Override
        public void onDatastoreStatusChange(DbxDatastore dbxDatastore) {
            if (dbxDatastore.getSyncStatus().hasIncoming) {
                syncDropbox();
                getTables();

                Intent broadcast = new Intent();
                broadcast.setAction(Constants.DB_SYNC_REQUIRED);
                context.sendBroadcast(broadcast);
            }
        }
    };

    public DbxTable getTable(String table) {
        return datastore.getTable(table);
    }
}
