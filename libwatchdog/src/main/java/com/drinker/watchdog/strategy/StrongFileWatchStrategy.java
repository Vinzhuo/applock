package com.drinker.watchdog.strategy;

import android.content.Context;
import android.util.Log;


import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.strategy.boot.BasePolicy;
import com.drinker.watchdog.util.PrefUtils;

import java.io.File;

/**
 * Created by liuzhuolin on 16/9/19.
 */
public class StrongFileWatchStrategy extends FileWatchStrategy {
    
    private final String CHILD_START_TIME_SP_KEY = "child_start_time";
    private final String CHILD_START_COUNT_SP_KEY = "child_start_count";

    public StrongFileWatchStrategy(BasePolicy policy) {
        super(policy);
    }

    @Override
    public void onWorkerCreate(final Context context, final Configurations configs) {
        if (configs != null) {
            mContext = context;
            mCreateType = CREATE_TYPE_WORKER;
            initWatchFiles(context);
            final boolean start = mPolicy.onStartRemainer(context, configs, true);
            Thread t = new Thread() {
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
                    File dir = context.getDir(FILE_WATCH_DIR, Context.MODE_PRIVATE);
                    StrongFileWatchStrategy.this.start(new File(dir, FILE_WATCH_A1), new File(dir,
                            FILE_WATCH_B1), new File(dir, FILE_WATCH_A2), new File(dir, FILE_WATCH_B2), false);
                };
            };
            t.start();
            if (configs.listener != null) {
                mConfigurations = configs;
                configs.listener.onWorkerWatched(context);
            }
        } else {
            Log.d(TAG, "[FileWatchStrategy]->onWorkerCreate(), configs is null.");
        }
    }

    @Override
    public void onRemainerCreate(final Context context, final Configurations configs) {
        if (configs != null) {
            mContext = context;
            mCreateType = CREATE_TYPE_REMAINER;
            final boolean start = mPolicy.onStartWorker(context, configs, true);
            Thread t = new Thread() {
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
                    File dir = context.getDir(FILE_WATCH_DIR, Context.MODE_PRIVATE);
                    StrongFileWatchStrategy.this.start(new File(dir, FILE_WATCH_B1), new File(dir,
                            FILE_WATCH_A1), new File(dir, FILE_WATCH_B2), new File(dir, FILE_WATCH_A2), true);
                };
            };
            t.start();
            if (configs.listener != null) {
                mConfigurations = configs;
                configs.listener.onRemainerWatched(context);
            }
        } else {
            Log.d(TAG, "[FileWatchStrategy]->onRemainerCreate(), configs is null.");
        }
    }

    @Override
    public void onWatchDied() {
        final boolean start = mPolicy.onStartPeer();
        if (start) {
            /**
             * 优化监听到对方挂掉时，不自杀。
             */
            switch (mCreateType) {
                case CREATE_TYPE_WORKER:
                    onWorkerCreate(mContext, mConfigurations);
                    break;
                case CREATE_TYPE_REMAINER:
                    mPolicy.onStartWorker(mContext, mConfigurations, true);
                    Thread t = new Thread() {
                        public void run() {
                            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
                            File dir = mContext.getDir(FILE_WATCH_DIR, Context.MODE_PRIVATE);
                            StrongFileWatchStrategy.this.start(new File(dir, FILE_WATCH_B1), new File(dir,
                                    FILE_WATCH_A1), new File(dir, FILE_WATCH_B2), new File(dir, FILE_WATCH_A2));
                        };
                    };
                    t.start();
                    break;
                default:
                    android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
        if (mConfigurations != null && mConfigurations.listener != null) {
            mConfigurations.listener.onDied();
        }
    }

    //连续>MAX_RESTART_COUNT次 重启间隔 < MIN_RESTART_TIME_GAP reture false
    private boolean onChildRestart() {
        boolean needRestart = true;
        long lastTime = PrefUtils.getLong(mContext, CHILD_START_TIME_SP_KEY);
        long curTime = System.currentTimeMillis();
        int count = PrefUtils.getInt(mContext, CHILD_START_COUNT_SP_KEY);
        long gap = curTime - lastTime;
        if (gap >= 0 && gap < MIN_RESTART_TIME_GAP) {
            count++;
            PrefUtils.putInt(mContext, CHILD_START_COUNT_SP_KEY, count);
        } else {
            PrefUtils.putInt(mContext, CHILD_START_COUNT_SP_KEY, 0);
        }
        PrefUtils.putLong(mContext, CHILD_START_TIME_SP_KEY, curTime);
        if (count >= MAX_RESTART_COUNT) {
            needRestart = false;
            PrefUtils.putInt(mContext, CHILD_START_COUNT_SP_KEY, 0);
        }
        return needRestart;
    }
    
    public void onChildDied() {
        mPolicy.onStartPeer();
        mPolicy.onStartRemainer(mContext, mConfigurations, true);
        if (mConfigurations != null && mConfigurations.listener != null) {
            mConfigurations.listener.onDied();
        }
        if (onChildRestart()) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

}
