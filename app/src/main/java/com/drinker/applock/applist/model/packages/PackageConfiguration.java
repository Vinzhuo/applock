package com.drinker.applock.applist.model.packages;

import android.text.TextUtils;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.HashSet;

public final class PackageConfiguration {

    private static final String TAG = "PackageConfiguration";

    private static final String[] EXCLUDE_CONFIGURATIONS = {
            "com.google.android.gms", "com.ali.babasecurity.privacyknight", "com.google.android.launcher",
            "com.google.android.googlequicksearchbox"
    };

    private SparseArray<PkgRecommend> recommendSet;

    private final HashSet<String> excludeSet;

    protected PackageConfiguration() {
        excludeSet = new HashSet<>(Arrays.asList(EXCLUDE_CONFIGURATIONS));
    }

    public boolean containsRecommend(String packageName) {
        return getRecommend(packageName) != null;
    }

    public boolean containsRecommendWithValidData(String packageName) {
        PkgRecommend data = getRecommend(packageName);
        return data != null;
    }

    protected boolean containsExclude(String packageName) {
        return excludeSet.contains(packageName);
    }

    protected synchronized void fillRecommendData() {
        if (recommendSet == null) {
            recommendSet = new SparseArray<>(130);
        }
    }
    public synchronized PkgRecommend getRecommend(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        if (recommendSet == null) {
            recommendSet = new SparseArray<>(130);
        }
        PkgRecommend info = recommendSet.get(pkgName.hashCode());
        return info;
    }
}
