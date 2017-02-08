package com.drinker.alock.lock.manager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.text.TextUtils;


import com.drinker.alock.lock.annotations.LockAppStatusDef;
import com.drinker.alock.monitor.ActivityMonitorManager;
import com.drinker.alock.monitor.IActivityMonitorListener;
import com.drinker.alock.util.log.Log;
import com.drinker.alock.util.process.ProcessData;
import com.drinker.alock.util.process.ProcessUtils;
import com.drinker.core.util.DeviceUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhuolin on 15-7-28.
 */
final public class LockHelper implements IActivityMonitorListener {

    private static final String TAG = "LockHelper";

    private static volatile LockHelper sInstance = null;

    private ActivityMonitorManager monitorManager = null;

    final private Map<String, LockedApp> lockedAppMap = new HashMap<>();

    public static final String PACKAGE_NAME = "package_name";

    final private List<ILockListener> lockListeners = new LinkedList<>();

    private Context context;

    private String selfPackageName;

    private AppObserverManager appObserverManager = new AppObserverManager(this);

    private AppRecordManager appRecordManager;

    private LockAppConfiguration appConfiguration = null;

    private Persistence persistence;

    private AppStatusTrace appStatusTrace;

    private Map<String, ProcessData> lockPrcStatus = new HashMap<>(1); //5.1.1以及5.1.1以上使用

    private String lastTopPkgname = ""; // 5.1.1以及5.1.1以上使用

    private boolean needNewDoChange = false;

    private String nowTopPkgname = "";

    private long newTopTime = 0;
    private final static long BARRIER_TIME = 150;//ms

    private LockHelper() {
        monitorManager = ActivityMonitorManager.get();
    }

    public static LockHelper get() {
        if (sInstance != null) {
            return sInstance;
        }

        synchronized (LockHelper.class) {
            if (sInstance == null) {
                sInstance = new LockHelper();
            }
        }
        return sInstance;
    }

    public boolean init(Context ctx, Persistence persist) {
        if (ctx == null) {
            return false;
        }
        if (persist == null) {
            persistence = new Persistence(ctx) {
                @Override
                protected String decode(Context context, String stored) {
                    return null;
                }

                @Override
                protected String encode(Context context, String source) {
                    return null;
                }
            };
        } else {
            persistence = persist;
        }
        context = ctx;
        selfPackageName = context.getPackageName();
        appStatusTrace = new AppStatusTrace(context);
        appConfiguration = new LockAppConfiguration(context);
        initSelf();
        if ((context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)!=0) {
            Log.setLevel(android.util.Log.VERBOSE);
            Log.i(TAG, "is debug");
        }
        return true;
    }

    protected Context getContext() {
        return context;
    }

    private void initSelf() {
        initLockedApps();
        monitorManager.initManager(context);
        monitorManager.registerActivityMonitorListener(this);
        if (Build.VERSION.SDK_INT >= 21) {
            ActivityManager activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
            if (appProcessInfos == null || (appProcessInfos.size() < 6 )) {
                needNewDoChange = true;
            }
        } else if (Build.VERSION.SDK_INT < 21) {
            appRecordManager = new AppRecordManager();
        }
        if (needNewDoChange) {
            List<LockedApp> apps = getLockedApps();
            int size = apps.size();
            if (size > 0) {
                ArrayList<String> packageNames = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    packageNames.add(apps.get(i).getPackageName());
                }
                Collection<ProcessData> datas = ProcessUtils.getProcessInfo(packageNames.toArray(new String[0]));
                if (datas != null && datas.size() > 0) {
                    Map<String, ProcessData> map = new HashMap<>(datas.size());
                    for (ProcessData processData : datas) {
                        if (processData != null && !TextUtils.isEmpty(processData.packageName)) {
                            map.put(processData.packageName, processData);
                        }
                    }
                    lockPrcStatus = map;
                }
            }
        } else {
        }
    }

    private void initLockedApps() {
        Set<String> lockedPackages = persistence.getReadOnlyLockedPackages();
        for (String packageName : lockedPackages) {
            if (!TextUtils.isEmpty(packageName)) {
                LockedApp lockedApp = new LockedApp();
                lockedApp.setPackageName(packageName);
                lockedAppMap.put(packageName, lockedApp);
            }
        }
        LockedApp lockedApp = new LockedApp();
        lockedApp.setPackageName(selfPackageName);
        lockedApp.setNotify(false);
        lockedAppMap.put(lockedApp.getPackageName(), lockedApp);
    }

    public void startLock() {
        if (getLockedApps().size() <= 0 || (needNewDoChange && !DeviceUtils.HavaPermissionForUsageStats(context))) {
            return;
        }
        monitorManager.startMonitor();
    }

    public void stopLock() {
        monitorManager.stopMonitor();
    }

    public synchronized void addListener(ILockListener listener) {
        if (listener == null || lockListeners.contains(listener)) {
            return;
        }
        lockListeners.add(listener);
    }

    public synchronized void removeListener(ILockListener listener) {
        if (listener == null || !lockListeners.contains(listener)) {
            return;
        }
        lockListeners.remove(listener);
    }

    public synchronized ILockListener[] getLockListeners() {
        ILockListener[] tmpLockListeners;
        tmpLockListeners = lockListeners.toArray(new ILockListener[0]);
        return tmpLockListeners;
    }

    public synchronized List<LockedApp> getLockedApps() {
        ArrayList<LockedApp> lockedApps = new ArrayList<>();
        Collection<LockedApp> lockedApps1 = lockedAppMap.values();
        for (LockedApp lockedApp : lockedApps1) {
            if (!selfPackageName.equals(lockedApp.getPackageName())) {
                lockedApps.add(lockedApp);
            }
        }
        return lockedApps;
    }

    private synchronized List<LockedApp> getApps() {
        ArrayList<LockedApp> lockedApps = new ArrayList<>();
        Collection<LockedApp> lockedApps1 = lockedAppMap.values();
        lockedApps.addAll(lockedApps1);
        return lockedApps;
    }

    private synchronized void addLockedApp(String packageName) {
        LockedApp lockedApp = new LockedApp();
        lockedApp.setPackageName(packageName);
        lockedAppMap.put(packageName, lockedApp);
    }

    private synchronized void removeLockedApp(String packageName) {
        lockedAppMap.remove(packageName);
    }

    private synchronized LockedApp getLockedApp(String packageName) {
        return lockedAppMap.get(packageName);
    }

    private synchronized void updateLockedAppStatus(String packageName, @LockAppStatusDef int status) {
        LockedApp lockedApp;
        lockedApp = lockedAppMap.get(packageName);
        if (lockedApp == null) {
            return;
        }
        lockedApp.setStatus(status);
    }

    private synchronized void addPrcStatus(String ... pkgName) {
        Collection<ProcessData> datas = ProcessUtils.getProcessInfo(pkgName);
        if (datas != null && datas.size() > 0) {
            for (ProcessData data : datas) {
                if (data != null) {
                    lockPrcStatus.put(data.packageName, data);
                }
            }
        }
    }

    private synchronized void removePrcStatus(String pkgName) {
        lockPrcStatus.remove(pkgName);
    }

    synchronized ProcessData getPrcStatus(String pkgName) {
        return lockPrcStatus.get(pkgName);
    }

    private synchronized void updatePrcStatus(ProcessData ... datas) {
        for (ProcessData data : datas) {
            if (data != null && !TextUtils.isEmpty(data.packageName)) {
                ProcessData t =  lockPrcStatus.get(data.packageName);
                if (t != null) {
                    data.oomChangeValue = data.oomScore - t.oomScore;
                } else {
                    data.oomChangeValue = 0;
                }
                if (data.oomChangeValue > 0) {
                    data.oomOrientation = ProcessData.OOM_UP;
                } else if (data.oomChangeValue < 0) {
                    data.oomOrientation = ProcessData.OOM_DOWN;
                } else {
                    data.oomOrientation = ProcessData.OOM_INIT;
                }
                lockPrcStatus.put(data.packageName, data);
            }
        }
    }

    public void lock(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        boolean success = persistence.lock(packageName);
        if (!success) {
            return;
        }
        addLockedApp(packageName);
        if (needNewDoChange) {
            addPrcStatus(packageName);
        }

    }

    public void lock(String[] packageNames) {
        if (packageNames == null) {
            return;
        }
        persistence.lock(packageNames);
        for (String packageName : packageNames) {
            if (!TextUtils.isEmpty(packageName)) {
                addLockedApp(packageName);
            }
        }
        if (needNewDoChange) {
            addPrcStatus(packageNames);
        }
    }

    public synchronized void clearLock() {
        boolean result = persistence.clearLock();
        if (result) {
            lockedAppMap.clear();
            LockedApp lockedApp = new LockedApp();
            lockedApp.setPackageName(selfPackageName);
            lockedApp.setNotify(false);
            lockedAppMap.put(lockedApp.getPackageName(), lockedApp);
        }
    }

    public void unLock(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        persistence.unlock(packageName);
        removeLockedApp(packageName);
        if (needNewDoChange) {
            removePrcStatus(packageName);
        }
    }

    public boolean isLocked(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        return getLockedApp(packageName) != null;
    }

    public void setLockedAppStatus(String packageName, @LockAppStatusDef int status) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
        if (!packageName.equals(selfPackageName) && status == LockedApp.UNLOCK) {
            appObserverManager.watch(packageName);
        }
        updateLockedAppStatus(packageName, status);
    }

    private synchronized boolean isNotifyLock(String packageName, String activityName) {
        int status = getLockedAppStatus(packageName);
        return status == LockedApp.BACKGROUND || status == LockedApp.LOCKING;
    }

    synchronized public void resetAppNotify() {
        List<LockedApp> lockedApps = getLockedApps();
        for (LockedApp lockedApp : lockedApps) {
            lockedApp.setNotify(true);
        }
        if (appStatusTrace != null) {
            appStatusTrace.clear();
        }
    }

    synchronized public void resetAppsStatus(String packageName) {
        Log.e(TAG, "resetAppsStatus: " + packageName);
        List<LockedApp> lockedApps = getApps();
        LockedApp app = lockedAppMap.get(packageName);
        int clearFlag = 0;
        if (app == null) {
            clearFlag = 1;
        } else if (app.getStatus() == LockedApp.LOCKING || app.getStatus() == LockedApp.UNLOCK) {
            clearFlag = 2;
        } else if (app.getStatus() == LockedApp.BACKGROUND) {
            if (!app.needLock() && app.getPackageName().equals(packageName) && !selfPackageName.equals(packageName)) {
                setAppNeedLock(app.getPackageName(), true);
                setLockedAppStatus(app.getPackageName(), LockedApp.UNLOCK);
                clearFlag = 3;
            }
        }
        if (clearFlag != 0 && lockedApps != null) {
            for (LockedApp lockedApp : lockedApps) {
                if (clearFlag == 1 || (!packageName.equals(lockedApp.getPackageName()))) {
                    if (!selfPackageName.equals(lockedApp.getPackageName())) {
                        setLockedAppStatus(lockedApp.getPackageName(), LockedApp.BACKGROUND);
                    }
                }
            }
        }
    }

    public Persistence getPersistence() {
        return persistence;
    }

    synchronized public int getLockedAppStatus(String packageName) {
        LockedApp lockedApp;
        lockedApp = lockedAppMap.get(packageName);
        if (lockedApp == null) {
            return -1;
        }
        return lockedApp.getStatus();
    }

    synchronized public boolean isNeedNotify(String pkg) {
        LockedApp lockedApp;
        lockedApp = lockedAppMap.get(pkg);
        if (lockedApp == null) {
            return false;
        }
        return lockedApp.needNotify();
    }

    synchronized private boolean isNeedLock(String pkg) {
        LockedApp lockedApp;
        lockedApp = lockedAppMap.get(pkg);
        if (lockedApp == null) {
            return false;
        }
        return lockedApp.needLock();
    }

    private void  doOnchangedOld(String s, String s1) {
        if (appRecordManager != null) {
            appRecordManager.onTopAppChange(s, s1);
        }
        notifyListener(s, s1);
        resetAppsStatus(s);
    }

    private void doOnChangedNew() {
        String s = "";
        boolean isopen = DeviceUtils.HavaPermissionForUsageStats(context);
        if (!isopen) {
            List<LockedApp> lockedApps = getLockedApps();
            int size = lockedApps.size();
            if (size <= 0) {
                return;
            }
            ArrayList<String> packageNames = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                packageNames.add(lockedApps.get(i).getPackageName());
            }
            List<ProcessData> processData = ProcessUtils.getTopProcess(packageNames.toArray(new String[0]));
            ProcessData p = getBestProcess(processData);
            if (p == null || TextUtils.isEmpty(p.packageName)) {
                resetAppsStatus("");
                return;
            }
            s = p.packageName;
        } else {
            s = ProcessUtils.queryUsageStats(context);
        }
        Log.i(TAG, "new, top:" + s);
        if (!appConfiguration.isValidPackageName(s)) {
            return;
        }
        if (!TextUtils.isEmpty(s)) {
            if (validChanged(s)) {
                notifyListener(s, "");
                resetAppsStatus(s);
            }
        }
    }

    private void notifyListener(String pkg, String activityName) {
        ILockListener[] tmpLockListeners = getLockListeners();
        for (ILockListener lockListener : tmpLockListeners) {
            lockListener.onTopAppChange(pkg, activityName);
        }
        if (appStatusTrace != null && !appStatusTrace.isValid(pkg)) {
            setLockedAppStatus(pkg, LockedApp.UNLOCK);
            return;
        }
        int status = getLockedAppStatus(pkg);
        boolean needLock = isNeedLock(pkg);

        if ((status == LockedApp.BACKGROUND || status == LockedApp.LOCKING) && needLock) {
            // 防止进程被杀后，重新拉活反复锁
            boolean needlockForWhatsapp = true;
            if (appRecordManager != null) {
                needlockForWhatsapp = appRecordManager.needLock();
            }
            if (!needlockForWhatsapp) {
                setLockedAppStatus(pkg, LockedApp.PRESENTED);
            } else {
                for (ILockListener lockListener : tmpLockListeners) {
                    lockListener.onLockedAppPresent(pkg, activityName);
                }
            }
        }
    }

    public synchronized void setAppNeedLock(String packageName, boolean need) {
        if (TextUtils.isEmpty(packageName) || packageName.equals(selfPackageName)) {
            return;
        }
        LockedApp lockedApp;
        lockedApp = lockedAppMap.get(packageName);
        if (lockedApp == null) {
            return;
        }
        lockedApp.setLock(need);
    }

    public synchronized void setLockedAppNoti(String packageName, boolean notify) {
        if (TextUtils.isEmpty(packageName) || packageName.equals(selfPackageName)) {
            return;
        }
        LockedApp lockedApp;
        lockedApp = lockedAppMap.get(packageName);
        if (lockedApp == null) {
            return;
        }
        lockedApp.setNotify(notify);
    }
    @Override
    public void onChanged(String s, String s1) {
//        if (!DeviceUtils.isUserPresent(context)) {
//            return;
//        }
        if (needNewDoChange) {
            doOnChangedNew();
        } else {
            Log.i(TAG, "old top: " + s + " activityname : " + s1);
            if (!TextUtils.isEmpty(s) &&
                    validChanged(s) && appConfiguration.isValidActivity(s1)) {
                if (appConfiguration.isValidPackageName(s)) {
                    Log.i(TAG, "old top in: " + s);
                    doOnchangedOld(s, s1);
                } else if (appConfiguration.isSystemPhone(s)){
                    doDialerFix();
                }
            }
        }
    }

    private void doDialerFix() {
        List<String> pkgs = appConfiguration.getSystemDialers();
        for (String pkg : pkgs) {
            setLockedAppStatus(pkg, LockedApp.UNLOCK);
        }
    }

    private boolean validChanged(String pkg) {
        if (!pkg.equals(nowTopPkgname)) {
            long now = System.currentTimeMillis();
            long gap = now - newTopTime;
            if (gap > BARRIER_TIME) {
                nowTopPkgname = pkg;
                newTopTime = now;
                return true;
            }
        }
        return false;
    }

    public void onChanged() {
        if (needNewDoChange) {
            doOnChangedNew();
        } else {
            ActivityMonitorManager.TopActivityInfo topActivityInfo = monitorManager.getTopActivityInfo();
            doOnchangedOld(topActivityInfo.packageName, topActivityInfo.topActivityName);
        }
    }

    private ProcessData extractProcessData(List<ProcessData> processDatas, String pkg) {
        for (ProcessData data : processDatas) {
            if (data.packageName.equals(pkg) && data.oomChangeValue <= 100) {
                return data;
            }
        }
        return null;
    }

    private ProcessData getBestProcess(List<ProcessData> processDatas) {
        ProcessData processData = new ProcessData();
        if (processDatas == null || processDatas.size() <= 0) {
            if (!TextUtils.isEmpty(lastTopPkgname)) {
                addPrcStatus(lastTopPkgname);
                lastTopPkgname = "";
            }
            return processData;
        }
        updatePrcStatus(processDatas.toArray(new ProcessData[0]));
        processData = processDatas.get(0);
        int size = processDatas.size();
        if (size > 1) {
            Collections.sort(processDatas);
            processData = processDatas.get(0);
            Log.e("getBestProcess", "size : " + processDatas.size());
            Log.e("getBestProcess", "oomName : " + processData.packageName + " stableName : " + processDatas.get(1).packageName);
            Log.e("getBestProcess", "oomScore : " + processData.oomScore + " stableOom : " + processDatas.get(1).oomScore);
        }

        if (size > 1 && processData != null && !TextUtils.isEmpty(processData.packageName)) {
            ProcessData data = getPrcStatus(processData.packageName);
            if (((data.oomChangeValue >= -30 && data.oomChangeValue <= 0) || data.oomOrientation == ProcessData.OOM_UP) &&
                    !data.packageName.equals(lastTopPkgname)) {

                processData = extractProcessData(processDatas, lastTopPkgname);
            }
        }
        if (processData != null) {
            if (!TextUtils.isEmpty(lastTopPkgname) && !lastTopPkgname.equals(processData.packageName)) {
                addPrcStatus(lastTopPkgname);
            }
            lastTopPkgname = processData.packageName;
        } else {
            if (!TextUtils.isEmpty(lastTopPkgname)) {
                addPrcStatus(lastTopPkgname);
            }
            lastTopPkgname = "";
        }
        Log.e("getBestProcess", "top Name : " + lastTopPkgname);
        return processData;
    }

    public boolean needUsageStatPermission() {
        return needNewDoChange;
    }

    public boolean needOpenUsageStatPermission() {
        if (needNewDoChange && Build.VERSION.SDK_INT >= 19 && !DeviceUtils.HavaPermissionForUsageStats(context)) {
            return true;
        } else {
            return false;
        }
    }



}
