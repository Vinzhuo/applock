/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class WatchDogApp extends Application {
    private static final String TAG = WatchDog.TAG;

    private boolean mAttached = false;
    private BufferedReader mBufferedReader;//release later to save time

    @Override
    protected final void attachBaseContext(Context base) {
        if (mAttached) {
            return;
        }
        mAttached = true;
        super.attachBaseContext(base);

        /**
         *
         */
        initWatchDog(base);

        /**
         *
         */
        attachContext(base);
    }

    private void initWatchDog(Context context) {
        Configurations.Configuration workConfigure = null;
        if (Build.VERSION.SDK_INT >= 21) {
            JobScheduler jobScheduler = (JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1111, new ComponentName(this, KeepaliveService.class));
            builder.setPeriodic(TimeUnit.MINUTES.toMillis(5)); //150000 -> 5 minutes
            builder.setPersisted(true);
            final int result = jobScheduler.schedule(builder.build());
        }
        if (Build.VERSION.SDK_INT >= 11) {
            AccountSyncAdapter.prepare(this);
        }
        final boolean support = WatchDog.isSuportWatchDog(context);
        if (support) {
            workConfigure = onWatchDogConfig();
        }
        if (workConfigure != null) {
            String processName = getProcessName();
            Configurations.Configuration remainConfigure = new Configurations.Configuration(".watchdog", WatchDogService.class, WatchdogReceiver.class);
            Configurations configs = new Configurations(workConfigure, remainConfigure);
            //Log.d(TAG, "initWatchDog() processName: " + processName);
            if(processName.startsWith(configs.wokerConfig.processName)) {
                WatchDog.fetch().onWorkerCreate(context, configs);
            }else if(processName.startsWith(configs.remainConfig.processName)){
                WatchDog.fetch().onRemainerCreate(context, configs);
            }
        }
    }

    private String getProcessName() {
        String pname = "";
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            mBufferedReader = new BufferedReader(new FileReader(file));
            pname = mBufferedReader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mBufferedReader != null) {
                try {
                    mBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mBufferedReader = null;
        }
        return pname;
    }


    /**
     *
     * @return
     */
    protected abstract Configurations.Configuration onWatchDogConfig();


    protected void attachContext(Context context) {

    }
}
