package com.drinker.core.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zhuolin on 15-11-3.
 */
public class PreferenceUtils {

    public static final String PREF_FILE_COMMON = "prefs.common";

    public static int getInt(Context context, String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_COMMON, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, -1);
    }
    public static void putInt(Context context, String key, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE_COMMON, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

}
