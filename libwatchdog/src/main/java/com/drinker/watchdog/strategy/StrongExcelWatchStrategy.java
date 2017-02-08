package com.drinker.watchdog.strategy;

import android.content.ComponentName;
import android.content.Context;
import android.os.Process;
import android.util.Log;


import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.strategy.boot.BasePolicy;

import java.io.File;

/**
 * Created by liuzhuolin on 16/9/26.
 */

public class StrongExcelWatchStrategy extends ExcelpWatchStrategy {
    public StrongExcelWatchStrategy(BasePolicy policy) {
        super(policy);
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
                    StrongExcelWatchStrategy.this.start(componentName, binaryFile, true);
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
    public void onWatchDied() {
        mPolicy.onStartRemainer(context, configs, true);
        if (onRemainerRestart(context)) {
            Thread t = new Thread() {
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
                    File binaryFile = new File(context.getDir(BIN_DEST_DIR_NAME, Context.MODE_PRIVATE),
                            BIN_FILE_NAME);
                    ComponentName componentName = new ComponentName(context.getPackageName(), configs.remainConfig.service.getName());
                    StrongExcelWatchStrategy.this.start(componentName, binaryFile, true);
                };
            };
            t.start();
        }
        if (configs.listener != null) {
            configs.listener.onWorkerWatched(context);
        }
    }

    public void onChildDied() {
    }
}
