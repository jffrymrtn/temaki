package com.jmartin.temaki.model;

/**
 * Created by jeff on 2013-09-03.
 */
public class Constants {
    public static final int EMPTY_LIST_ITEMS_COUNT = 0;

    public static final int NEW_LIST_ID = 0;
    public static final int RENAME_LIST_ID = 1;

    public static final String FOCUS_TITLE = "Focus";
    public static final String ALERT_DIALOG_TAG = "delete_confirmation_dialog_fragment";
    public static final String INPUT_DIALOG_TAG = "generic_name_dialog_fragment";
    public static final String LIST_ITEMS_BUNDLE_KEY = "ListItems";
    public static final String LIST_NAME_BUNDLE_KEY = "ListName";

    public static final String DEFAULT_LIST_NAME = "NEW LIST ";
    public static final String LISTS_SP_KEY = "MAIN_LISTS";
    public static final String LAST_OPENED_LIST_SP_KEY = "last_opened_list";
    public static final String FOCUS_SP_KEY = "focus_key";
    public static final String FOCUS_BUNDLE_ID = "focus_bundle_id";

    public static String KEY_PREF_LOCALE = "pref_locale_option";
    public static String KEY_PREF_DROPBOX_SYNC = "pref_sync_option";
    public static String KEY_PREF_STARTUP_OPTION = "pref_startup_option";
    public static String KEY_PREF_LIST_ITEMS_CAPITALIZE_OPTION = "pref_list_items_capitalize_option";
    public static String KEY_PREF_RATE_TEMAKI = "pref_rate_temaki_option";
    public static String KEY_PREF_SYNC = "pref_sync";

    // 0-49, task IDs
    public static final int CANCEL_RESULT_CODE = 0;
    public static final int DELETE_ITEM_ID = 1;
    public static final int EDIT_ITEM_ID = 2;
    public static final int EDIT_FOCUS_ID = 3;
    public static final int FOCUS_ACTIVITY_RESULT_ID = 4;

    // 50+ Sync IDs
    public static final int DBX_LINK_REQUEST_ID = 50;

    public static final String INTENT_RESULT_KEY = "ResultKey";

    // API Keys - Replace these with actual values before launch
    public static final String DB_APP_SECRET = "";
    public static final String DB_APP_KEY = "";

    public static final String TABLE_ITEM_TITLE = "title";
    public static final String TABLE_ITEM_ISFINISHED = "isFinished";
    public static final String TABLE_ITEM_ISHIGHLIGHTED = "isHighlighted";
}
