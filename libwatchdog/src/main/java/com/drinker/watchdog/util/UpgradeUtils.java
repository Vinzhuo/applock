package com.drinker.watchdog.util;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by liuzhuolin on 16/9/22.
 */
final public class UpgradeUtils {

    private static final String VERSION_CODE = "version_code";

    public static final boolean excelBinNeedUpgrade(Context context) {
        int lastVersion = PrefUtils.getInt(context, VERSION_CODE);
        int version = getVersionCode(context);
        if (version > lastVersion) {
            PrefUtils.putInt(context, VERSION_CODE, version);
        }
        return version > lastVersion;
    }

    private static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
            return -1;
        }
    }
}
