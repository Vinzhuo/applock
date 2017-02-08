package com.drinker.alock.lock.manager;

import android.content.Context;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;


import com.drinker.alock.monitor.ActivityMonitorManager;
import com.drinker.alock.util.log.Log;
import com.drinker.alock.util.process.ProcessUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zhuolin on 15-10-13.
 */
final class AppObserverManager {

    private static final int TIME_GAP = 10; //MS

    private static final int TIME_MAX = 800; //MS

    private String TAG = "AppObserverManager";

    private LockHelper lockHelper = null;

    private AppObserver appObserver = null;

    private int pid = 0;

    private String packageName = "";

    Handler handler = null;

    int timer = 0;

    AppObserverManager(LockHelper l) {
        lockHelper = l;
        HandlerThread handlerThread = new HandlerThread("AppObserverManager");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void watch(String p) {
        if (TextUtils.isEmpty(p)) {
            return;
        }
        if (appObserver != null) {
            appObserver.stopWatching();
            appObserver = null;
        }
        String path = getPath(p, lockHelper.getContext());
        if (TextUtils.isEmpty(path)) {
            return;
        }
        timer = 0;
        packageName = p;
        appObserver = new AppObserver(path);
        appObserver.startWatching();
    }

    private class AppObserver extends FileObserver {

        public AppObserver(String path) {
            super(path, 4038);
        }

        @Override
        public void onEvent(int event, String path) {
            if (getOomAdj() > 0) {
                if (handler != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ActivityMonitorManager.TopActivityInfo activityInfo = ActivityMonitorManager.get().getTopActivityInfo();
                            if (activityInfo == null || TextUtils.isEmpty(packageName)) {
                                return;
                            }
                            if (!activityInfo.packageName.equals(packageName)) {
                                stopWatching();
                                Log.i(TAG, "appstatuschanged packagename : " + activityInfo.packageName);
                                lockHelper.onChanged(activityInfo.packageName, activityInfo.topActivityName);
                            } else {
                                if (timer < TIME_MAX) {
                                    timer += TIME_GAP;
                                    handler.postDelayed(this, TIME_GAP);
                                } else {
                                    stopWatching();
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    private int getOomAdj() {
        BufferedReader in = null;
        int oomAdj = 0;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(File.separatorChar + "proc" + File.separatorChar + pid + File.separatorChar + getOomName())));
            String line = in.readLine();
            if (!TextUtils.isEmpty(line)) {
                oomAdj = Integer.valueOf(line);
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return oomAdj;
    }

    private String getOomName() {
//        if (Build.VERSION.SDK_INT < 23) {
//            return "oom_score_adj";
//        } else {
//            return "oom_score";
//        }
        return "oom_score_adj";
    }

    private String getPath(String packageName, Context ctx) {
        pid = ProcessUtils.getProcessPid(ctx, packageName);
        String oomName = "oom_adj";
        if (Build.VERSION.SDK_INT > 20) {
            oomName = "oom_score_adj";
        }
//        if (Build.VERSION.SDK_INT >= 23) {
//            oomName = "oom_score";
//        }
        return File.separatorChar + "proc" + File.separatorChar + pid + File.separatorChar + oomName;
    }
}
