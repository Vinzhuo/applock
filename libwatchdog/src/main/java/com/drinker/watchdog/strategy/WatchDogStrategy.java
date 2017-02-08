/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog.strategy;

import android.content.ComponentName;
import android.content.Context;


import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.strategy.boot.BasePolicy;
import com.drinker.watchdog.util.PrefUtils;

import java.io.File;



public abstract class WatchDogStrategy {

    protected static final int MIN_RESTART_TIME_GAP = 500;//ms
    protected static final int MAX_RESTART_COUNT = 5;

    private final String REMIANER_START_TIME_SP_KEY = "child_start_time";
    private final String REMIANER_START_COUNT_SP_KEY = "child_start_count";

    protected BasePolicy mPolicy;


    public WatchDogStrategy(BasePolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("watchdog stratety param of policy don't null.");
        }
        mPolicy = policy;
    }

    /**
     *
     * @param context
     * @return
     */
    public abstract boolean onInitialization(Context context);


    //连续>MAX_RESTART_COUNT次 重启间隔 < MIN_RESTART_TIME_GAP reture false
    protected boolean onRemainerRestart(Context context) {
        boolean needRestart = true;
        long lastTime = PrefUtils.getLong(context, REMIANER_START_TIME_SP_KEY);
        long curTime = System.currentTimeMillis();
        int count = PrefUtils.getInt(context, REMIANER_START_COUNT_SP_KEY);
        long gap = curTime - lastTime;
        if (gap >= 0 && gap < MIN_RESTART_TIME_GAP) {
            count++;
            PrefUtils.putInt(context, REMIANER_START_COUNT_SP_KEY, count);
        } else {
            PrefUtils.putInt(context, REMIANER_START_COUNT_SP_KEY, 0);
        }
        PrefUtils.putLong(context, REMIANER_START_TIME_SP_KEY, curTime);
        if (count >= MAX_RESTART_COUNT) {
            needRestart = false;
            PrefUtils.putInt(context, REMIANER_START_COUNT_SP_KEY, 0);
        }
        return needRestart;
    }

    /**
     *
     * @param context
     * @param configs
     */
    public abstract void onWorkerCreate(Context context, Configurations configs);


    /**
     *
     * @param context
     * @param configs
     */
    public abstract void onRemainerCreate(Context context, Configurations configs);


    /**
     *
     */
    protected abstract void onWatchDied();

    /**
     *
     * @param componentName
     * @param executableFile
     * @param need
     */
    final protected void start(ComponentName componentName, File executableFile, boolean need) {
        start(componentName.getPackageName(), componentName.getClassName(), executableFile.getAbsolutePath(), need);
    }

    /**
     *
     * @param componentName
     * @param executableFile
     */
    final protected void start(ComponentName componentName, File executableFile) {
        start(componentName.getPackageName(), componentName.getClassName(), executableFile.getAbsolutePath(), false);
    }

    /**
     *
     * @param a1
     * @param b1
     * @param a2
     * @param b2
     */
    final protected void start(File a1, File b1, File a2, File b2) {
        start(a1.getAbsolutePath(), b1.getAbsolutePath(), a2.getAbsolutePath(), b2.getAbsolutePath(), false);
    }

    /**
     *
     * @param a1
     * @param b1
     * @param a2
     * @param b2
     * @param need
     */
    final protected void start(File a1, File b1, File a2, File b2, boolean need) {
        start(a1.getAbsolutePath(), b1.getAbsolutePath(), a2.getAbsolutePath(), b2.getAbsolutePath(), need);
    }

    /**
     *
     * @param packageName
     * @param serviceName
     * @param executablePath
     */
    final protected native void start(String packageName, String serviceName, String executablePath, boolean need);

    /**
     *
     * @param a1Path
     * @param b1Path
     * @param a2Path
     * @param b2Path
     * @param needStrong
     */
    final protected native void start(String a1Path, String b1Path, String a2Path, String b2Path, boolean needStrong);

    static public  void test() {

    }

    static {
        try {
            System.loadLibrary("watchdog");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
