/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class Configurations {
    public static Configuration wokerConfig;
    public static Configuration remainConfig;
    public final WatchDogListener listener;

    public Configurations(Configuration wokerConfig, Configuration remainConfig) {
        this(wokerConfig, remainConfig, null);
    }

    public Configurations(Configuration wokerConfig, Configuration remainConfig,
                          WatchDogListener listener) {
        this.wokerConfig = wokerConfig;
        this.remainConfig = remainConfig;
        this.listener = listener;
    }

    public static void startWorkService(Context context) {
        if (wokerConfig != null) {
            Intent intent = new Intent(context, wokerConfig.service);
            context.startService(intent);
        }
    }

    public static class Configuration {

        public final String processName;
        public final Class<? extends Service> service;
        public final Class<? extends BroadcastReceiver> receiver;

        public Configuration(String processName, Class<? extends Service> service,
                Class<? extends BroadcastReceiver> receiver) {
            this.processName = processName;
            this.service = service;
            this.receiver = receiver;
        }
    }


    public interface WatchDogListener {

        /**
         * push进程准备完成回调
         * @param context
         */
        void onWorkerWatched(Context context);

        /**
         * 看护进程准备完成回调
         * @param context
         */
        void onRemainerWatched(Context context);

        /**
         * 进程死亡回调
         */
        void onDied();
    }
}
