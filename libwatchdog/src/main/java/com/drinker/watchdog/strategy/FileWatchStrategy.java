/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog.strategy;

import android.content.Context;
import android.util.Log;


import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.WatchDog;
import com.drinker.watchdog.strategy.boot.BasePolicy;

import java.io.File;
import java.io.IOException;


public class FileWatchStrategy extends WatchDogStrategy {
    protected static final String TAG = WatchDog.TAG;

    protected static final String FILE_WATCH_DIR = "files";

    protected static final String FILE_WATCH_A1 = "watchdog_a1";

    protected static final String FILE_WATCH_B1 = "watchdog_b1";

    protected static final String FILE_WATCH_A2 = "watchdog_a2";

    protected static final String FILE_WATCH_B2 = "watchdog_b2";

    protected static final int CREATE_TYPE_UNKONW = 0x00;

    protected static final int CREATE_TYPE_WORKER = 0x01;

    protected static final int CREATE_TYPE_REMAINER = 0x02;


    protected Context mContext;
    protected int mCreateType = CREATE_TYPE_UNKONW;
    protected Configurations mConfigurations;

    public FileWatchStrategy(BasePolicy policy) {
        super(policy);
    }

    @Override
    public boolean onInitialization(Context context) {
        return initWatchFiles(context);
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
                    FileWatchStrategy.this.start(new File(dir, FILE_WATCH_A1), new File(dir,
                            FILE_WATCH_B1), new File(dir, FILE_WATCH_A2), new File(dir, FILE_WATCH_B2));
                }
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
                    FileWatchStrategy.this.start(new File(dir, FILE_WATCH_B1), new File(dir,
                            FILE_WATCH_A1), new File(dir, FILE_WATCH_B2), new File(dir, FILE_WATCH_A2));
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
                    onRemainerCreate(mContext, mConfigurations);
                    break;
                default:
                    android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
        if (mConfigurations != null && mConfigurations.listener != null) {
            mConfigurations.listener.onDied();
        }
    }

    protected boolean initWatchFiles(Context context){
        try {
            File dirFile = context.getDir(FILE_WATCH_DIR, Context.MODE_PRIVATE);
            if(!dirFile.exists()){
                dirFile.mkdirs();
            }
            createNewFile(dirFile, FILE_WATCH_A1);
            createNewFile(dirFile, FILE_WATCH_B1);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createNewFile(File dirFile, String fileName) throws IOException {
        File file = new File(dirFile, fileName);
        if(!file.exists()){
            file.createNewFile();
        }
    }
}
