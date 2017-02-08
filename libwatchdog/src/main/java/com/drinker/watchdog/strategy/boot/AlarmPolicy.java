/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog.strategy.boot;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.WatchDog;


public class AlarmPolicy extends BasePolicy {

    private static final String TAG = WatchDog.TAG;


    private AlarmManager mAlarmManager;

    private PendingIntent mPendingIntent;

    @Override
    public boolean onStartWorker(Context context, Configurations configurations, boolean force) {
        if (configurations != null && configurations.wokerConfig != null) {
            return startService(context, configurations.wokerConfig.service, force);
        }
        return false;
    }

    @Override
    public boolean onStartRemainer(Context context, Configurations configurations, boolean force) {
        if (configurations != null && configurations.remainConfig != null) {
            return startService(context, configurations.remainConfig.service, force);
        }
        return false;
    }

    @Override
    public boolean onStartPeer() {
        if (mAlarmManager != null) {
            mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                    100, mPendingIntent);
        }
        return true;
    }


    private boolean startService(Context context, Class<? extends Service> service, boolean force) {
        boolean success = false;
        if (force == true && context != null && service != null) {
            Intent intent = getServiceIntent(context, service);
            ComponentName result = context.startService(intent);
            success = (result != null);
        }

        if (force == true && success == true) {
            initAlarm(context, service);
        }
        return force == true ? success: true;
    }

    private void initAlarm(Context context, Class<? extends Service> service){
        if(mAlarmManager == null){
            mAlarmManager = ((AlarmManager)context.getSystemService(Context.ALARM_SERVICE));
        }
        if(mPendingIntent == null){
            Intent intent = getServiceIntent(context, service);
            mPendingIntent = PendingIntent.getService(context, 0, intent, 0);
        }
        mAlarmManager.cancel(mPendingIntent);
    }
}
