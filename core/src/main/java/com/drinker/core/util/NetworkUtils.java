package com.drinker.core.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;


/**
 * Created by SamuraiSong on 10/8/15.
 */
public class NetworkUtils {

    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static String getActiveNetworkType(Context context) {
        if (context == null) {
            return "no";
        }
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return "no";
        }
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo == null) {
            return "no";
        }
        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int mobileType = getMobileType(telephonyManager.getNetworkType());
            if (mobileType == MobileType.MOBILE_4G) {
                return "4G";
            } else if (mobileType == MobileType.MOBILE_3G) {
                return "3G";
            } else if (mobileType == MobileType.MOBILE_2G) {
                return "2G";
            }
        } else if (networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
            return "WIFI";
        }
        return "no";
    }

    public static boolean isWifiActive(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    public static boolean isMobileActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    public static boolean isMobile2GActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int mobileType = getMobileType(telephonyManager.getNetworkType());
            if (mobileType == MobileType.MOBILE_2G) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMobile3GActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int mobileType = getMobileType(telephonyManager.getNetworkType());
            if (mobileType == MobileType.MOBILE_3G) {
                return true;
            }
        }
        return false;
    }

    public static boolean isMobile4GActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }
        if (networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            int mobileType = getMobileType(telephonyManager.getNetworkType());
            if (mobileType == MobileType.MOBILE_4G) {
                return true;
            }
        }
        return false;
    }

    private static int getMobileType(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return MobileType.MOBILE_2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return MobileType.MOBILE_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
                return MobileType.MOBILE_4G;
            default:
                return MobileType.MOBILE_UNKNOWN;
        }
    }

    private static class MobileType {
        public static final int MOBILE_2G = 0;
        public static final int MOBILE_3G = 1;
        public static final int MOBILE_4G = 2;
        public static final int MOBILE_UNKNOWN = 3;

    }

}
