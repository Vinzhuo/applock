package com.drinker.applock.applist.model.packages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;


import com.drinker.core.util.IntentUtils;

import java.lang.ref.WeakReference;

public final class PackageReceiver extends BroadcastReceiver {

    private static final String TAG = "PackageReceiver";

    public static final String INTENT_DATA_SCHEME = "package";

    private final WeakReference<PackageContainer> manageWeak;

    PackageReceiver(PackageContainer manager) {
        manageWeak = new WeakReference<>(manager);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!IntentUtils.isValid(intent)) {
            return;
        }

        if (context == null) {
            return;
        }

        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        String packageName = "";
        if (intent.getData() != null) {
            packageName = intent.getData().getSchemeSpecificPart();
        }

        int what;
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            what = PackageContainer.MSG_PACKAGE_ADDED;
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            what = PackageContainer.MSG_PACKAGE_REMOVED;
        } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            what = PackageContainer.MSG_PACKAGE_REPLACED;
        } else if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            what = PackageContainer.MSG_PACKAGE_CHANGED_FORCIBLY;
        } else {
            return;
        }

        PackageContainer container = manageWeak.get();
        if (container != null) {
            container.trigger(what, packageName);
        }
    }
}
