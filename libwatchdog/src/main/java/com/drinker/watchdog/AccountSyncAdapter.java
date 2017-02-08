package com.drinker.watchdog;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

/**
 * Created by liuzhuolin on 2016/11/7.
 */
public class AccountSyncAdapter {

    private static final String TAG = "SyncAdapter";

    private static final String ACCOUNT_TYPE = "com.drinker.account";

    public static final String ACCOUNT_FROM_VALUE = "sync";

    private static final int SYNC_INTERVAL = 5 * 60; //S

    /**
     * 初始化
     * @param context
     */
    public static void prepare(Context context) {
        Account account = getAccount(context);
        if (account == null) {
            final String name = getAccountName(context);
            account = addAccount(context, name);
        }
        if (account != null) {
            ContentResolver.setSyncAutomatically(account, DrinkAccountProvider.AUTHORITY, true);
            ContentResolver.addPeriodicSync(account, DrinkAccountProvider.AUTHORITY, Bundle.EMPTY,
                    SYNC_INTERVAL); //定期
        }

    }

    /**
     * 手动刷新同步
     * @param context
     */
    public static void refresh(Context context) {
        Account account = getAccount(context);
        if (account != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            ContentResolver.requestSync(account, DrinkAccountProvider.AUTHORITY, bundle);
        }
    }

    /**
     * 增加账号
     * @return
     */
    private static Account addAccount(Context context, final String name) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(name, ACCOUNT_TYPE);
        final boolean result = accountManager.addAccountExplicitly(account, "", Bundle.EMPTY);
        return result ? account : null;
    }

    /**
     * 获取已经存在的账号
     * @param context
     * @return
     */
    private static Account getAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts != null && accounts.length > 0) {
            return accounts[0];
        }
        return null;
    }

    private static String getAccountName(Context context) {
        return context.getResources().getString(R.string.app_name);
    }

    /**
     * Define a Service that returns an IBinder for the sync adapter class,
     * allowing the sync adapter framework to call onPerformSync().
     */
    public static class SyncService extends Service {

        // Storage for an instance of the sync adapter
        private static SyncAdapterInner sSyncAdapter = null;

        // Object to use as a thread-safe lock
        private static final Object sSyncAdapterLock = new Object();

        @Override
        public void onCreate() {
            super.onCreate();
            /*
             * Create the sync adapter as a singleton. Set the sync adapter as
             * syncable Disallow parallel syncs
             */
            synchronized (sSyncAdapterLock) {
                if (sSyncAdapter == null) {
                    sSyncAdapter = new SyncAdapterInner(getApplicationContext(), true);
                }
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            /*
             * Get the object that allows external processes to call
             * onPerformSync(). The object is created in the base class code
             * when the SyncAdapter constructors call super()
             */
            return sSyncAdapter.getSyncAdapterBinder();
        }

        /**
         * Handle the transfer of data between a server and an
         * app, using the Android sync adapter framework.
         */
        private class SyncAdapterInner extends AbstractThreadedSyncAdapter {
            // Define a variable to contain a content resolver instance
            private ContentResolver mContentResolver;

            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public SyncAdapterInner(Context context, boolean autoInitialize,
                                    boolean allowParallelSyncs) {
                super(context, autoInitialize, allowParallelSyncs);

                /*
                 * If your app uses a content resolver, get an instance of it
                 * from the incoming Context
                 */
                mContentResolver = context.getContentResolver();

            }


            public SyncAdapterInner(Context context, boolean autoInitialize) {
                super(context, autoInitialize);

                /*
                 * If your app uses a content resolver, get an instance of it
                 * from the incoming Context
                 */
                mContentResolver = context.getContentResolver();
            }

            @Override
            public void onPerformSync(Account account, Bundle extras, String authority,
                                      ContentProviderClient provider, SyncResult syncResult) {

            }
        }
    }


    /**
     * A bound Service that instantiates the authenticator when started.
     */
    public static class AuthenticatorService extends Service {

        // Instance field that stores the authenticator object
        private Authenticator mAuthenticator;

        @Override
        public void onCreate() {
            super.onCreate();

            // Create a new authenticator object
            mAuthenticator = new Authenticator(this);
        }

        /*
         * When the system binds to this Service to make the RPC call return the
         * authenticator's IBinder.
         */
        @Override
        public IBinder onBind(Intent intent) {
            return mAuthenticator.getIBinder();
        }

        /*
         * Implement AbstractAccountAuthenticator and stub out all of its
         * methods
         */
        private class Authenticator extends AbstractAccountAuthenticator {

            public Authenticator(Context context) {
                super(context);
            }

            @Override
            public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
                return null;
            }

            @Override
            public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                                     String authTokenType, String[] requiredFeatures, Bundle options)
                    throws NetworkErrorException {
                return null;
            }

            @Override
            public Bundle confirmCredentials(AccountAuthenticatorResponse response,
                                             Account account, Bundle options) throws NetworkErrorException {
                return null;
            }

            @Override
            public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                                       String authTokenType, Bundle options) throws NetworkErrorException {
                return null;
            }

            @Override
            public String getAuthTokenLabel(String authTokenType) {
                return null;
            }

            @Override
            public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                                            String authTokenType, Bundle options) throws NetworkErrorException {
                return null;
            }

            @Override
            public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                                      String[] features) throws NetworkErrorException {
                return null;
            }
        }

    }

    /*
     * Define an implementation of ContentProvider that stubs out all methods
     */
    public static class DrinkAccountProvider extends ContentProvider {

        public static final String AUTHORITY = "com.drinker.provider.sync";

        private static final String CONTENT_URI_BASE = "content://" + AUTHORITY;

        private static final String TABLE_NAME = "data";

        public static final Uri CONTENT_URI = Uri.parse(CONTENT_URI_BASE + "/" + TABLE_NAME);

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            return null;
        }

        @Override
        public String getType(Uri uri) {
            return "";
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            return null;
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            return 0;
        }
    }

}
