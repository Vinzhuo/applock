package com.drinker.alock.monitor.activitymonitor;

import android.os.RemoteException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zhuolin on 15-7-24.
 */
public class OldActivityMonitor extends android.app.IActivityWatcher.Stub implements IActivityMonitor {

    private IWatcher watcher = null;

    @Override
    public void activityResuming(int activityId) throws RemoteException {
        if (watcher != null) {
            watcher.onChanged();
        }
    }

    @Override
    public void closingSystemDialogs(String reason) throws RemoteException {
    }

    @Override
    public void start() {
        attachWatcher(this);
    }

    @Override
    public void stop() {
        detachWatcher(this);
    }

    public void setListener(IWatcher wat) {
        watcher = wat;
    }

    private void attachWatcher(android.app.IActivityWatcher paramIActivityWatcher) {
        try {
            Class<?> activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            activityManagerNative = Class.forName("android.app.ActivityManagerNative");
            Method getDefaultMethod = activityManagerNative.getMethod("getDefault");
            Object iActivityManager = getDefaultMethod.invoke((Object[]) null, (Object[]) null);
            if (iActivityManager != null) {
                Method registerMethod = activityManagerNative.getMethod("registerActivityWatcher", new Class[]{android.app.IActivityWatcher.class});
                registerMethod.invoke(iActivityManager, paramIActivityWatcher);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void detachWatcher(android.app.IActivityWatcher paramIActivityWatcher) {
        Class localClass = null;
        try {
            localClass = Class.forName("android.app.ActivityManagerNative");
            Object localObject = localClass.getMethod("getDefault", new Class[0]).invoke(null, new Object[0]);
            localClass.getMethod("unregisterActivityWatcher", new Class[] { android.app.IActivityWatcher.class }).invoke(localObject, new Object[] { paramIActivityWatcher });
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
