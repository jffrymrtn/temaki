package com.jmartin.temaki.sync;

import android.app.Activity;
import android.content.Context;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileStatus;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.jmartin.temaki.model.Constants;

import java.io.IOException;

/**
 * Created by jeff on 2013-09-06.
 */
public class SyncManager {

    private static Context context;

    private static DbxAccountManager dbxAcctMgr;
    private static DbxFileSystem dbxFs;
    private static DbxPath metadataPath;
    private static DbxFile metadataFile;


    public static void init(Context ctx) {
        context = ctx;
        dbxAcctMgr = DbxAccountManager.getInstance(context, Constants.DB_APP_KEY, Constants.DB_APP_SECRET);

        if (dbxAcctMgr.hasLinkedAccount()) {
            setupDropboxFileSystem();
        }
    }

    public static void linkDropboxAccount(Activity activity) {
        dbxAcctMgr.startLink(activity, Constants.DBX_LINK_REQUEST_ID);
    }

    public static void setupDropboxFileSystem() {
        try {
            dbxFs = DbxFileSystem.forAccount(dbxAcctMgr.getLinkedAccount());
            metadataPath = new DbxPath(Constants.METADATA_FILENAME);

            if (dbxFs.exists(metadataPath)) {
                loadMetadata();
            } else {
                dbxFs.create(metadataPath);
            }
        } catch (DbxException.Unauthorized unauthorized) {
            // TODO Handle unauthorized access... maybe re-authenticate?
        } catch (DbxException e) {
            // TODO Handle
        }
    }

    private static void loadMetadata() {
        try {
            metadataFile = dbxFs.open(metadataPath);

            // Assign change listener to metadataFile
            registerFileListener();
        } catch (DbxException e) {
            // TODO Handle
        }
    }

    private static DbxFileStatus getFileStatus(DbxFile dbxFile) {
        // TODO watch out for this null value
        DbxFileStatus result = null;
        try {
            result = dbxFile.getSyncStatus();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static DbxFileStatus getNewerFileStatus(DbxFile dbxFile) {
        // TODO watch out for this null value
        DbxFileStatus result = null;
        try {
            result = dbxFile.getNewerStatus();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void syncToDropbox() {

    }

    public static void loadFromDropbox() {

    }

    public static void deregisterFileListener() {
        metadataFile.removeListener(metadataFileListener);
    }

    public static void registerFileListener() {
        DbxFileStatus metadataFileStatus = getFileStatus(metadataFile);
        if (!metadataFileStatus.isCached) {
            metadataFile.addListener(metadataFileListener);
        }
    }

    /**
     * Get the metadata JSON String from Dropbox.
     */
    public static String getMetadataJsonString() {
        String result = "";
        try {
            result = metadataFile.readString();
        } catch (IOException e) {
            // TODO handle exception
        }
        return result;
    }

    private static DbxFile.Listener metadataFileListener = new DbxFile.Listener() {
        @Override
        public void onFileChange(DbxFile dbxFile) {
            DbxFileStatus metadataFileStatus = getFileStatus(metadataFile);
            if (!metadataFileStatus.isLatest) {
                DbxFileStatus newerStatus = getNewerFileStatus(metadataFile);
                if (newerStatus.isCached) {
                    try {
                        metadataFile.update();
                        // TODO Somehow automatically update ListView with content...
                    } catch (DbxException e) {
                        // TODO Handle this
                    }
                }
            }
        }
    };
}
