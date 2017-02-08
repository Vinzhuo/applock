package com.drinker.alock.lock.manager;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by zhuolin on 15-8-22.
 */
final class AppRecordManager implements ILockListener {


    private AppRecord appRecord = new AppRecord();

    private Set<String> adustProducts = new HashSet<>();


    protected AppRecordManager () {
        adustProducts.add("com.whatsapp");
    }


    private String getBaseActivityName(String pkg) {
        if (TextUtils.isEmpty(pkg)) {
            return "";
        }
        ActivityManager manager = (ActivityManager) LockHelper.get().getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List localList = manager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo localRunningTaskInfo = (ActivityManager.RunningTaskInfo)localList.get(0);
        if (localRunningTaskInfo != null) {
            ComponentName info = localRunningTaskInfo.topActivity;
            if (info != null && pkg.equals(info.getPackageName())) {
                return localRunningTaskInfo.baseActivity == null ? "" : localRunningTaskInfo.baseActivity.getPackageName();
            }
        }
        return "";
    }

    public synchronized boolean needLock() {
        AppRecord.PkgRecord trecord = appRecord.peek();
        AppRecord.PkgRecord srecord = appRecord.get(1);
        if (trecord == null || srecord == null) {
            return true;
        }
        if (!srecord.isNeedLock()) {
            srecord.setNeedLock(true);
            return false;
        }
        String topPkg = trecord.getPkgname();
        String bpkg = getBaseActivityName(topPkg);
        if (bpkg.equals(srecord.getPkgname())) {
            trecord.setNeedLock(false);
            return false;
        }
        return true;
    }


    private synchronized void updateTopAppRecord(String packageName) {
        AppRecord.PkgRecord record = new AppRecord.PkgRecord();
        record.setPkgname(packageName);
        appRecord.push(record);
    }


    @Override
    public void onLockedAppPresent(String packageName, String activityName) {

    }

    @Override
    public void onTopAppChange(String packageName, String activityName) {
        updateTopAppRecord(packageName);
    }
}
