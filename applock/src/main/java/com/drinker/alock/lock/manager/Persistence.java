package com.drinker.alock.lock.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhuolin on 16/4/12.
 */
public abstract class Persistence {

    private static final String TAG = Persistence.class.getSimpleName();

    private static final String NAME_PERSISTENCE = "packages";

    private static final String KEY_LOCKED = "locked";

    private static final String SPLIT_REGULAR = "\\|";

    private static final String SPLIT_FLAG = "|";

    private final Context context;

    private HashSet<String> packages;

    protected Persistence(Context context) {
        this.context = context;
        refreshLockedPackages(context);
    }

    public synchronized boolean clearLock() {
        packages.clear();
        HashSet<String> set = new HashSet<>(packages);
        return commit(set);
    }

    public synchronized final Set<String> getReadOnlyLockedPackages() {
        return new HashSet<>(packages);
    }

    public synchronized final boolean isLocked(String packageName) {
        return !TextUtils.isEmpty(packageName) && packages.contains(packageName);
    }

    protected synchronized HashSet<String> refreshLockedPackages(Context context) {
        HashSet<String> set = new HashSet<>();
        SharedPreferences preferences = context.getSharedPreferences(NAME_PERSISTENCE, Context.MODE_PRIVATE);
        do {
            if (preferences == null) {
                break;
            }
            String stored = preferences.getString(KEY_LOCKED, "");
            if (TextUtils.isEmpty(stored)) {
                break;
            }
            String combined = decode(context, stored);
            if (TextUtils.isEmpty(combined)) {
                combined = stored;
            }
            set.clear();
            String[] array = combined.split(SPLIT_REGULAR);
            set.addAll(Arrays.asList(array));
        } while (false);
        packages = set;
        return set;
    }

    public synchronized final boolean lock(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        HashSet<String> set = new HashSet<>(packages);
        return set.add(packageName) && commit(set);
    }

    public synchronized final int lock(String[] packageNames) {
        if (packageNames == null || packageNames.length == 0) {
            return 0;
        }

        HashSet<String> set = new HashSet<>(packages);
        int count = 0;
        for (String name : packageNames) {
            if (TextUtils.isEmpty(name)) {
                continue;
            }
            if (set.add(name)) {
                ++count;
            }
        }

        return commit(set) ? 0 : count;
    }

    public synchronized final boolean unlock(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }

        HashSet<String> set = new HashSet<>(packages);
        return set.remove(packageName) && commit(set);
    }

    protected synchronized boolean commit(HashSet<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String name : set) {
            sb.append(name).append(SPLIT_FLAG);
        }
        String combined = "";
        int length = sb.length();
        if (length > SPLIT_FLAG.length()) {
            combined = sb.substring(0, length - SPLIT_FLAG.length());
        }
        String stored = encode(context, combined);
        if (TextUtils.isEmpty(stored)) {
            stored = combined;
        }

        SharedPreferences preferences = context.getSharedPreferences(NAME_PERSISTENCE, Context.MODE_PRIVATE);
        if (preferences == null) {
            return false;
        }
        SharedPreferences.Editor editor = preferences.edit();
        boolean successful = editor.putString(KEY_LOCKED, stored).commit();
        if (successful) {
            packages = set;
        }
        return successful;
    }

    abstract protected String decode(Context context, String stored);

    abstract protected String encode(Context context, String source);
}
