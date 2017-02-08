/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog.strategy;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;


import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.WatchDog;
import com.drinker.watchdog.strategy.boot.BasePolicy;
import com.drinker.watchdog.util.UpgradeUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class ExcelpWatchStrategy extends WatchDogStrategy {

    protected static final String TAG = WatchDog.TAG;

    protected final String BIN_DEST_DIR_NAME = "bin";
    protected final String BIN_FILE_NAME = "watchdogd";
    protected Context context;
    protected Configurations configs;

    public ExcelpWatchStrategy(BasePolicy policy) {
        super(policy);
    }

    @Override
    public boolean onInitialization(Context context) {
        return installBin(context);
    }

    @Override
    public void onWorkerCreate(final Context context, final Configurations configs) {
        this.context = context;
        this.configs = configs;
        if (configs != null) {
            installBin(context);
            Thread t = new Thread() {
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                    File binaryFile = new File(context.getDir(BIN_DEST_DIR_NAME, Context.MODE_PRIVATE),
                            BIN_FILE_NAME);
                    ComponentName componentName = new ComponentName(context.getPackageName(), configs.remainConfig.service.getName());
                    ExcelpWatchStrategy.this.start(componentName, binaryFile);
                }
            };
            //t.setPriority(Thread.MAX_PRIORITY);
            t.start();
            if (configs.listener != null) {
                configs.listener.onWorkerWatched(context);
            }
        } else {
            Log.d(TAG, "[BinWatchStrategy]->onWorkerCreate(), configs is null.");
        }

    }

    @Override
    public void onRemainerCreate(Context context, Configurations configs) {
        if (configs != null) {
            mPolicy.onStartWorker(context, configs, true);
            if (configs.listener != null) {
                configs.listener.onDied();
            }
            /**
             * remainer进程需要自杀
             */
            Process.killProcess(Process.myPid());
        } else {
            Log.d(TAG, "[BinWatchStrategy]->onRemainerCreate(), configs is null.");
        }
    }

    @Override
    public void onWatchDied() {
        mPolicy.onStartRemainer(context, configs, true);
        if (onRemainerRestart(context)) {
            Thread t = new Thread() {
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                    File binaryFile = new File(context.getDir(BIN_DEST_DIR_NAME, Context.MODE_PRIVATE),
                            BIN_FILE_NAME);
                    ComponentName componentName = new ComponentName(context.getPackageName(), configs.remainConfig.service.getName());
                    ExcelpWatchStrategy.this.start(componentName, binaryFile);
                };
            };
            t.start();
        }
        if (configs.listener != null) {
            configs.listener.onWorkerWatched(context);
        }
    }

    /**
     *
     * @param context
     * @return
     */
    protected boolean installBin(Context context) {
        final String binaryDirName = "armeabi";
        return install(context, BIN_DEST_DIR_NAME, binaryDirName, BIN_FILE_NAME);
    }


    /**
     *
     * @param context
     * @param destDirName
     * @param assetsDirName
     * @param filename
     * @return
     */
    private boolean install(Context context, String destDirName, String assetsDirName,
                            String filename) {
        try {
            File file = new File(context.getDir(destDirName, Context.MODE_PRIVATE), filename);
            if (file.exists() && !UpgradeUtils.excelBinNeedUpgrade(context)) {
                return true;
            }
            copyAssets(context, (TextUtils.isEmpty(assetsDirName) ? ""
                    : (assetsDirName + File.separator)) + filename, file, "700");
            return true;
        } catch (Exception e) {
            Log.e("BinWatchStrategy", e.getMessage());
            return false;
        }
    }


    /**
     *
     * @param context
     * @param assetsFilename
     * @param file
     * @param mode
     * @throws IOException
     * @throws InterruptedException
     */
    private void copyAssets(Context context, String assetsFilename, File file, String mode)
            throws IOException, InterruptedException {
        AssetManager manager = context.getAssets();
        final InputStream is = manager.open(assetsFilename);
        copyFile(file, is, mode);
    }

    /**
     *
     * @param file
     * @param is
     * @param mode
     * @throws IOException
     * @throws InterruptedException
     */
    private void copyFile(File file, InputStream is, String mode) throws IOException,
            InterruptedException {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        final String abspath = file.getAbsolutePath();
        final FileOutputStream out = new FileOutputStream(file);
        byte buf[] = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        is.close();
        Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
    }

}
