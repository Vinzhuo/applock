package com.drinker.watchdog.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

/**
 * Created by liuzhuolin on 16/9/22.
 */
final public class PrefUtils {
    public static final String PREF_FILE = "prefs.watchdog";

    public static final int getInt(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, -1);
    }
    public static final void putInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static final long getLong(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, -1);
    }
    public static final void putLong(Context context, String key, long value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }
}
