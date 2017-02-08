package com.drinker.core.util;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Created by zhuolin on 16/4/11.
 */
public class DeviceUtils {

    private static final String TAG = DeviceUtils.class.getSimpleName();

    public static boolean isScreenUnlock(Context context) {
        if (context == null) {
            return false;
        }
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean flag = keyguardManager.inKeyguardRestrictedInputMode();
        return !flag;
    }

    public static boolean isScreenOn(Context context) {
        if (context == null) {
            return false;
        }
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return powerManager.isScreenOn();
    }

    public static boolean isUserPresent(Context context) {
        if (context == null) {
            return false;
        }
        return isScreenOn(context) && isScreenUnlock(context);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean HavaPermissionForUsageStats(Context context) {
        if (context == null) {
            return false;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName);
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getDeviceToken(Context context) {
        if (context == null) {
            return "";
        }
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = null;
        try {
            deviceId = tm.getDeviceId();
        } catch (SecurityException e) {
        }

        if (deviceId == null) {
            deviceId = "";
        }

        android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        String mac = null;

        try {
            WifiInfo wifiInfo = wifi.getConnectionInfo();
            if (wifiInfo != null) {
                mac = wifiInfo.getMacAddress();
            }
        } catch (SecurityException e) {
        }

        if (mac == null) {
            mac = "";
        }

        String androidId = android.provider.Settings.Secure.getString(context.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);

        if (androidId == null) {
            androidId = "";
        }

        return getMd5String(androidId + deviceId + mac);
    }

    private static final String getMd5String(String val) {
        if (TextUtils.isEmpty(val)) {
            return "";
        }
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes("UTF-8"));
            return getString(md5.digest());
        } catch (NoSuchAlgorithmException e) {
        } catch (UnsupportedEncodingException e) {
        }
        return "";
    }

    public static boolean hasSimCard(Context context) {
        boolean result = true;
        if (context != null) {
            TelephonyManager telMgr = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telMgr.getSimState();
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    result = false; //没有SIM卡
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    result = false; //未知
                    break;
            }
        }
        return result;
    }

    private static String getString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte bb : b) {
            sb.append(bb);
        }
        return sb.toString();
    }

    public static String getLocation(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        return locale.getCountry();
    }

    public static String getLanguage(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        return locale.getLanguage();
    }

    public static String getCurrentVersionName(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
                    .versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return versionName;
    }

}
