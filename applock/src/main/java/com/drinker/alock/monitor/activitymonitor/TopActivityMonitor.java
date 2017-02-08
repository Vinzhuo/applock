package com.drinker.alock.monitor.activitymonitor;

import android.os.Handler;
import android.os.HandlerThread;

import com.drinker.alock.util.log.Log;


/**
 * Created by zhuolin on 15-1-19.
 */
public class TopActivityMonitor implements IActivityMonitor {

    private static final String TAG = "TopActivityMonitor";
    private Handler handler = null;
    private IWatcher listener = null;
    private volatile boolean isBegin = false;
    private volatile long period = SHORT_PERIOD_MS;
    public static final long LONG_PERIOD_MS = 300;//单位:ms
    public static final long SHORT_PERIOD_MS = 150;//单位:ms


    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e(TAG, ex.getMessage());
            stop();
            start();
        }
    };

    public void setPeriod(long pd) {
        period = pd;
    }

    public long getPeriod() {
        return period;
    }

    public synchronized void start() {
            if (isBegin) {
                return;
            }
            Log.i(TAG, "monitor start");
            isBegin = true;
            HandlerThread handlerThread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
            handlerThread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (listener != null) {
                            listener.onChanged();
                            if (handler != null) {
                                long pd = period;
                                if (pd < SHORT_PERIOD_MS) {
                                    pd = SHORT_PERIOD_MS;
                                }
                                handler.postDelayed(this, pd);
                            }
                        }
                    } catch (Throwable thr) {
                        Log.e(TAG, thr.getMessage());
                        stop();
                        start();
                    }

                }
            });
    }

    public synchronized void stop() {
            Log.i(TAG, "monitor stop");
            isBegin = false;
            if (handler != null) {
                handler.removeCallbacksAndMessages(null);
                handler.getLooper().quit();
                handler = null;
            }
    }

    @Override
    public void setListener(IWatcher watcher) {
        listener = watcher;
    }

}
