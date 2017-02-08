package com.drinker.alock.monitor;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;


import com.drinker.alock.monitor.activitymonitor.OldActivityMonitor;
import com.drinker.alock.monitor.activitymonitor.DefaultActivityMonitor;
import com.drinker.alock.monitor.activitymonitor.IActivityMonitor;
import com.drinker.alock.monitor.activitymonitor.IWatcher;
import com.drinker.alock.monitor.activitymonitor.TopActivityMonitor;
import com.drinker.alock.util.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuolin on 15-7-23.
 */
final public class ActivityMonitorManager implements IWatcher {

    private static final String TAG = "ActivityMonitorManager";

    private static volatile ActivityMonitorManager sIntance = null;

    private IActivityMonitor activityMonitor = null;

    private List<IActivityMonitorListener> listeners = new ArrayList<IActivityMonitorListener>(1);

    private Context context = null;

    private MonitorConfiguration configuration = new MonitorConfiguration();

    private ActivityMonitorManager() {
        activityMonitor = getActivityMonitor();
        activityMonitor.setListener(this);
    }

    public static ActivityMonitorManager get() {
        if (sIntance == null) {
            synchronized (ActivityMonitorManager.class) {
                if (sIntance == null) {
                    sIntance = new ActivityMonitorManager();
                }
            }
        }
        return sIntance;
    }

    public void initManager(Context cxt) {
        context = cxt;
    }

    private IActivityMonitor getActivityMonitor() {
        IActivityMonitor monitor;
        String bandModel = Build.BRAND + " " + Build.MODEL;
        if (Build.VERSION.SDK_INT > 23 || configuration.isSpeical(bandModel)) {
            monitor = new TopActivityMonitor();
        } else if (Build.VERSION.SDK_INT <= 15) {
            monitor = new OldActivityMonitor();
        } else {
            monitor = new DefaultActivityMonitor();
        }
        return monitor;
    }

    public void registerActivityMonitorListener(IActivityMonitorListener listener) {
        synchronized (listeners) {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void unRegisterActivityMonitorListener(IActivityMonitorListener listener) {
        synchronized (listeners) {
            if (listener != null && listeners.contains(listener)) {
                listeners.remove(listener);
            }
        }
    }

    public void startMonitor() {
        activityMonitor.start();
    }

    public void stopMonitor() {
        activityMonitor.stop();
    }

    @Override
    public void onChanged() {
        TopActivityInfo topActivityInfo = getTopActivityInfo();
        synchronized (listeners) {
            for (IActivityMonitorListener listener : listeners) {
                listener.onChanged(topActivityInfo.packageName, topActivityInfo.topActivityName);
            }
        }
    }

    public static class TopActivityInfo {
        public String packageName = "";
        public String topActivityName = "";
    }

    public TopActivityInfo getTopActivityInfo() {
        TopActivityInfo info = new TopActivityInfo();
        if (context == null) {
            return info;
        }
        ActivityManager manager = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE));
        if (manager == null) {
            return info;
        }
        info = getTopActivityInfoByDefault(manager);
        if (TextUtils.isEmpty(info.packageName)) {
            info = getTopActivityInfoByInverse(manager);
        }
        return info;
    }

    private TopActivityInfo getTopActivityInfoByDefault(ActivityManager manager) {
        TopActivityInfo info = null;
        if (Build.VERSION.SDK_INT >= 21) {
            info = getTopActivityInfoByProcess(manager);
        } else {
            info = getTopActivityInfoByTask(manager);
        }
        return info;
    }

    private TopActivityInfo getTopActivityInfoByInverse(ActivityManager manager) {
        TopActivityInfo info = null;
        if (Build.VERSION.SDK_INT >= 21) {
            info = getTopActivityInfoByTask(manager);
        } else {
            info = getTopActivityInfoByProcess(manager);
        }
        return info;
    }

    private TopActivityInfo getTopActivityInfoByProcess(ActivityManager manager) {
        TopActivityInfo info = new TopActivityInfo();
        List<ActivityManager.RunningAppProcessInfo> pis = null;
        try {
            pis = manager.getRunningAppProcesses();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (pis == null || pis.size() <= 0) {
            return info;
        }
        ActivityManager.RunningAppProcessInfo topAppProcess = pis.get(0);
        if (topAppProcess != null && topAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            if (!TextUtils.isEmpty(topAppProcess.processName)) {
                int index = topAppProcess.processName.indexOf(":");
                if (index == -1) {
                    info.packageName = topAppProcess.processName;
                } else {
                    info.packageName = topAppProcess.processName.substring(0, index);
                }
            }
            info.topActivityName = "";
        }
        return info;
    }

    private TopActivityInfo getTopActivityInfoByTask(ActivityManager manager) {
        TopActivityInfo info = new TopActivityInfo();
        List localList = null;
        try {
            localList = manager.getRunningTasks(1);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (localList == null || localList.size() <= 0) {
            return info;
        }
        ActivityManager.RunningTaskInfo localRunningTaskInfo = (ActivityManager.RunningTaskInfo)localList.get(0);
        info.packageName = localRunningTaskInfo.topActivity.getPackageName();
        info.topActivityName = localRunningTaskInfo.topActivity.getClassName();
        return info;
    }

}