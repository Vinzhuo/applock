package com.drinker.applock.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.drinker.core.util.AppUtils;


public class PrefUtils {

    public static final String PREF_FILE_DEFAULT = "prefs.default";

    private static final SharedPreferences sPrefs = AppUtils.getApp().getSharedPreferences(PREF_FILE_DEFAULT,
        Context.MODE_PRIVATE);

    private static SharedPreferences.Editor sEditor;

    public static synchronized SharedPreferences.Editor editor() {
        if (sEditor == null) {
            sEditor = sPrefs.edit();
        }
        return sEditor;
    }


    public static SharedPreferences.Editor putString(String key, String value) {
        final SharedPreferences.Editor editor = editor();
        editor.putString(key, value);
        return editor;
    }

    public static SharedPreferences.Editor putStringSafely(String key, String value) {
        final SharedPreferences.Editor editor = editor();

        editor.putString(key, value);
        return editor;
    }

    public static String getStringSafely(String key) {
        String encode = sPrefs.getString(key, "");

        return encode;
    }



    /**
     *
     * @param key
     * @return
     */
    public static String getString(String key) {
        return sPrefs.getString(key, null);
    }

    /**
     * Get a string preference
     *
     * @param key
     * @param value
     */
    public static String getString(String key, String value) {
        return (sPrefs.contains(key)) ? sPrefs.getString(key, null) : value;
    }

    /**
     * Parse an Integer that is stored as a string
     *
     * @return The Integer or null if there was an error
     */
    public static Integer parseInt(String key) {
        try {
            return Integer.parseInt(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse an Integer that is stored as a String or get the default value
     * which is also stored as a String
     */
    public static Integer parseInt(String key, int value) {
        final Integer result = parseInt(key);
        return (result != null) ? result : value;
    }

    public static SharedPreferences.Editor putBoolean(String key, boolean value) {
        final SharedPreferences.Editor editor = editor();
        editor.putBoolean(key, value);
        return editor;
    }

    public static void putBooleanInMultiPorcess(Context ctx, String key, boolean value) {
        SharedPreferences sPrefs = ctx.getSharedPreferences("prefs.default", Context.MODE_MULTI_PROCESS);
        sPrefs.edit().putBoolean(key,value).apply();
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public static boolean getBoolean(String key, boolean value) {
        final Boolean result = getBooleanOrNull(key);
        return result != null ? result : value;
    }

    private static Boolean getBooleanOrNull(String key) {
        return (sPrefs.contains(key)) ? sPrefs.getBoolean(key, false) : null;
    }


    /**
     * After applying, call {@link #editor()} again.
     */
    public static void apply() {
        apply(editor());
        sEditor = null;
    }

    public static void apply(SharedPreferences.Editor editor) {
        if (editor == null) {
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    public static int getInt(String key) {
        return sPrefs.getInt(key, -1);
    }
    public static int getInt(String key, int defaultValue) {
        return sPrefs.getInt(key, defaultValue);
    }
    public static SharedPreferences.Editor putInt(String key, int value) {
        final SharedPreferences.Editor editor = editor();
        editor.putInt(key, value);
        return editor;
    }

    public static long getLong(String key) {
        return sPrefs.getLong(key, -1);
    }
    public static float getFloat(String key) {
        return sPrefs.getFloat(key, 0f);
    }
    public static SharedPreferences.Editor putLong(String key, long value) {
        final SharedPreferences.Editor editor = editor();
        editor.putLong(key, value);
        return editor;
    }
    public static SharedPreferences.Editor putFloat(String key, float value) {
        final SharedPreferences.Editor editor = editor();
        editor.putFloat(key, value);
        return editor;
    }
    public static boolean hasValue(String key) {
        return sPrefs.contains(key);
    }
}