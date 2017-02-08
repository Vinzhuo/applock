package com.drinker.alock.monitor.activitymonitor;

import android.os.Build;
import android.os.Handler;

import com.drinker.alock.util.log.Log;


/**
 * Created by zhuolin on 15-7-23.
 */
public class DefaultActivityMonitor implements IActivityMonitor {

    private static final String TAG = "DefaultActivityMonitor";
    private IWatcher watcher = null;

    private native void check(int version);
    private native void uncheck();

    private Handler handler = null;

    static {
        try {
            System.loadLibrary("monitor");
        } catch (Throwable e) {
            Log.e(TAG, "loadLibrary : " + e.getMessage());
        }
    }

    public DefaultActivityMonitor() {

    }

    public void onChanged() {
        watcher.onChanged();
    }

    @Override
    public void start() {
        try {
            check(Build.VERSION.SDK_INT);
        } catch (Throwable e) {
            Log.e(TAG, "loadLibrary : " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        try {
            uncheck();
        } catch (Throwable e) {
            Log.e(TAG, "loadLibrary : " + e.getMessage());
        }
    }

    @Override
    public void setListener(IWatcher wter) {
        watcher = wter;
    }
}
