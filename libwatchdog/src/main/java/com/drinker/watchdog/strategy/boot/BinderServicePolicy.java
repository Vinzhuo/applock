/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog.strategy.boot;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;


import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.WatchDog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;


public class BinderServicePolicy extends BasePolicy {
    private static final String TAG = WatchDog.TAG;

    private IBinder mRemote;
    private Parcel mServiceData;


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
        return transact();
    }


    private boolean startService(Context context, Class<? extends Service> service, boolean force) {
        initBinder();
        initServiceParcel(context, service);

        if (force) {
            return transact();
        } else {
            return true;
        }
    }

    private void initBinder() {
        if (mRemote != null) {
            return;
        }
        Class<?> activityManagerNative;
        try {
            activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Object amn = activityManagerNative.getMethod("getDefault")
                    .invoke(activityManagerNative);
            Field mRemoteField = amn.getClass().getDeclaredField("mRemote");
            mRemoteField.setAccessible(true);
            mRemote = (IBinder) mRemoteField.get(amn);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("Recycle")// when process dead, we should save time to restart and kill self, don`t take a waste of time to recycle
    private void initServiceParcel(Context context, Class<? extends Service> service) {
        if (mServiceData != null) {
            return;
        }
        Intent intent = getServiceIntent(context, service);
        // write pacel
        mServiceData = Parcel.obtain();
        mServiceData.writeInterfaceToken("android.app.IActivityManager");
        mServiceData.writeStrongBinder(null);
        // mServiceData.writeStrongBinder(callerBinder);
        intent.writeToParcel(mServiceData, 0);
        mServiceData.writeString(null);
        //适配华为 honor 6.0系统
        if (Build.VERSION.SDK_INT >= 23) {
            mServiceData.writeString(context.getPackageName());
        }
        // mServiceData.writeString(intent.resolveTypeIfNeeded(context.getContentResolver()));
        mServiceData.writeInt(0);
        // mServiceData.writeInt(handle);

    }

    private boolean transact() {
        try {
            if (mRemote == null || mServiceData == null) {
                Log.e(TAG, "remote or service is null.");
                return false;
            }
            mRemote.transact(34, mServiceData, null, 0); // START_SERVICE_TRANSACTION = 34
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
