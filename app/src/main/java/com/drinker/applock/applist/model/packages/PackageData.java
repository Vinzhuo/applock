package com.drinker.applock.applist.model.packages;

import android.content.pm.ApplicationInfo;

import com.drinker.alock.lock.manager.LockHelper;

public final class PackageData {

    private int type;
    private boolean isHighRecommend;
    private boolean locked;
    private String packageName;
    private String label;
    private String subLabel;
    private String pinyin;
    private PkgRecommend recommend;
    private int protectedCount = 0;
    private ApplicationInfo applicationInfo;
    public void setProtectedCount(int protectedCount) {
        this.protectedCount = protectedCount;
    }

    public void setGetIntruderCount(int getIntruderCount) {
        this.getIntruderCount = getIntruderCount;
    }

    public int getGetIntruderCount() {
        return getIntruderCount;
    }

    public int getProtectedCount() {
        return protectedCount;
    }

    private int getIntruderCount = 0;
    protected PackageData(int type, boolean locked, String
        packageName) {
        this.type = type;
        this.locked = locked;
        this.packageName = packageName;
        this.label = "";
        this.subLabel = "";
        this.pinyin = "";
    }

    protected PackageData(int type, boolean locked, String packageName, PkgRecommend rc) {
        this.type = type;
        this.locked = locked;
        this.packageName = packageName;
        this.label = "";
        this.subLabel = "";
        this.pinyin = "";
        this.recommend = rc;
    }

    public final PkgRecommend getRecommend() {
        return recommend;
    }

    public final boolean isLocked() {
        return LockHelper.get().isLocked(this.packageName);
    }

    public final void setHighRecommend(boolean highRecommend) {
        isHighRecommend = highRecommend;
    }

    public final boolean isHighRecommend() {
        return isHighRecommend;
    }

    public final PackageData setApplicationInfo(ApplicationInfo info) {
        this.applicationInfo = info;
        return this;
    }

    public final ApplicationInfo getApplicationInfo() {
        return this.applicationInfo;
    }


    public final PackageData setLocked(boolean locked) {
        this.locked = locked;
        return this;
    }

    public final int getType() {
        return type;
    }

    protected final PackageData setType(int type) {
        this.type = type;
        return this;
    }

    public final String getPackageName() {
        return packageName;
    }

    protected final PackageData setPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public final String getLabel() {
        return label;
    }

    protected final PackageData setLabel(String label) {
        this.label = label;
        return this;
    }

    public final String getSubLabel() {
        return subLabel;
    }

    protected final PackageData setSubLabel(String subLabel) {
        this.subLabel = subLabel;
        return this;
    }

    public final String getPinyin() {
        return pinyin;
    }

    protected final PackageData setPinyin(String pinyin) {
        this.pinyin = pinyin;
        return this;
    }
}
