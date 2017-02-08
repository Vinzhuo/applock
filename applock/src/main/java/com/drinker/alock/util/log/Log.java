package com.drinker.alock.util.log;

/**
 * Created by zhuolin on 16/4/13.
 */
final public class Log {

    private static ILog sLog = null;
    private static String sPrefix = "applock : ";
    private static int sLevel = android.util.Log.ERROR;
    public static void setLevel(int l) {
        sLevel = l;
    }
    public static int getLevel() {
        return sLevel;
    }
    public static void setPrefix(String prefix) {
        sPrefix = prefix;
    }
    public static String getPrefix() {
        return sPrefix;
    }
    public static void setLog(ILog log) {
        sLog = log;
    }
    public static ILog getLog() {
        return sLog;
    }
    public static int v(String tag, String msg) {
        if (sLevel > android.util.Log.VERBOSE) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.v(tag, msg);
        }
        return android.util.Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if (sLevel > android.util.Log.VERBOSE) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.v(tag, msg, tr);
        }
        return android.util.Log.v(tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        if (sLevel > android.util.Log.INFO) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.i(tag, msg);
        }
        return android.util.Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if (sLevel > android.util.Log.INFO) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.i(tag, msg, tr);
        }
        return android.util.Log.i(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        if (sLevel > android.util.Log.DEBUG) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.d(tag, msg);
        }
        return android.util.Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if (sLevel > android.util.Log.DEBUG) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.d(tag, msg, tr);
        }
        return android.util.Log.d(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        if (sLevel > android.util.Log.WARN) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.w(tag, msg);
        }
        return android.util.Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        if (sLevel > android.util.Log.WARN) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.w(tag, msg, tr);
        }
        return android.util.Log.w(tag, msg, tr);
    }

    public static int e(String tag, String msg) {
        if (sLevel > android.util.Log.ERROR) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.e(tag, msg);
        }
        return android.util.Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        if (sLevel > android.util.Log.ERROR) {
            return -1;
        }
        tag = sPrefix + tag;
        if (sLog != null) {
            return sLog.e(tag, msg, tr);
        }
        return android.util.Log.e(tag, msg, tr);
    }
}
