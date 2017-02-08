/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog.strategy.boot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;


import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.WatchDog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class BinderBroadcastPolicy extends BasePolicy {
    private static final String TAG = WatchDog.TAG;

    private IBinder mRemote;

    private Parcel mBroadcastData;

    @Override
    public boolean onStartWorker(Context context, Configurations configurations, boolean force) {
        if (configurations != null && configurations.wokerConfig != null) {
            return sendBroadcast(context, configurations.wokerConfig.receiver, force);
        }
        return false;
    }

    @Override
    public boolean onStartRemainer(Context context, Configurations configurations, boolean force) {
        if (configurations != null && configurations.remainConfig != null) {
            return sendBroadcast(context, configurations.remainConfig.receiver, force);
        }
        return false;
    }

    @Override
    public boolean onStartPeer() {
        return transact();
    }



    private boolean sendBroadcast(Context context, Class<? extends BroadcastReceiver> revc, boolean force) {
        initBinder();
        initBroadcastParcel(context, revc);
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
    private void initBroadcastParcel(Context context, Class<? extends BroadcastReceiver> revc) {
        if (mBroadcastData != null) {
            return;
        }
        Intent intent = getBroadcastIntent(context, revc);

        mBroadcastData = Parcel.obtain();
        mBroadcastData.writeInterfaceToken("android.app.IActivityManager");
        // mBroadcastData.writeStrongBinder(callerBinder);
        mBroadcastData.writeStrongBinder(null);
        intent.writeToParcel(mBroadcastData, 0);
        mBroadcastData.writeString(intent.resolveTypeIfNeeded(context.getContentResolver()));
        mBroadcastData.writeStrongBinder(null);
        mBroadcastData.writeInt(Activity.RESULT_OK);
        mBroadcastData.writeString(null);
        mBroadcastData.writeBundle(null);
        mBroadcastData.writeString(null);
        mBroadcastData.writeInt(-1);
        mBroadcastData.writeInt(0);
        mBroadcastData.writeInt(0);
        // mBroadcastData.writeInt(handle);
        mBroadcastData.writeInt(0);
    }

    private boolean transact() {

        try {
            if (mRemote == null || mBroadcastData == null) {
                Log.e(TAG, "remote or revc is null.");
                return false;
            }
            mRemote.transact(14, mBroadcastData, null, 0);// BROADCAST_INTENT_TRANSACTION = 0x00000001 + 13
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }
}
