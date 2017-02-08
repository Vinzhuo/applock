/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog.strategy.boot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.drinker.watchdog.Configurations;


public abstract class BasePolicy {

    public static final String WATCHDOG_FROM_VALUE = "watchdog";

    public static final int TYPE_WATCHDOG = 0x03;
    /**
     * @param context
     * @param configurations
     * @return
     */
    public abstract boolean onStartWorker(Context context, Configurations configurations,
            boolean force);

    /**
     * @param context
     * @param configurations
     * @return
     */
    public abstract boolean onStartRemainer(Context context, Configurations configurations,
            boolean force);

    /**
     * @return
     */
    public abstract boolean onStartPeer();


    /**
     *
     * @param context
     * @param service
     * @return
     */
    protected Intent getServiceIntent(Context context, Class<? extends Service> service) {
        return getIntent(context, service);
    }

    /**
     *
     * @param context
     * @param receiver
     * @return
     */
    protected Intent getBroadcastIntent(Context context, Class<? extends BroadcastReceiver> receiver) {
        return getIntent(context, receiver);
    }

    private Intent getIntent(Context context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("type", TYPE_WATCHDOG);
        intent.putExtra("from", WATCHDOG_FROM_VALUE);
        intent.putExtra("time", System.currentTimeMillis());
        //intent.putExtra("watchdog", WatchDog.isSuportWatchDog(context));
        return intent;
    }
}
