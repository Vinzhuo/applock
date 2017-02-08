package com.drinker.alock.lock.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Process;
import android.text.TextUtils;

import com.drinker.alock.util.log.Log;


/**
 * Created by zhuolin on 16/7/1.
 */
public class AppStatusTrace {
    private static final String TAG = AppStatusTrace.class.getSimpleName();
    private Context context;
    private SharedPreferences sharedPreferences;
    private int pid = 0;
    private String packageName = "";
    private static final String APP_PKG = "app_pkg";
    private static final String HOST_APP_PID = "host_app_pid";
    AppStatusTrace(Context cxt) {
        this.context = cxt;
        this.sharedPreferences = context.getSharedPreferences("app_status", Context.MODE_PRIVATE);
        pid = sharedPreferences.getInt(HOST_APP_PID, 0);
        packageName = sharedPreferences.getString(APP_PKG, "");
    }

    private void put(String pkg) {
        if (!TextUtils.isEmpty(pkg)) {
            pid = Process.myPid();
            packageName = pkg;
            sharedPreferences.edit()
                    .putString(APP_PKG, pkg)
                    .putInt(HOST_APP_PID, pid)
                    .apply();
            Log.i(TAG, " pid : " + Process.myPid() + " p: " + pkg);

        }
    }

    public boolean isValid(String pkg) {
        boolean result = true;
        if (!TextUtils.isEmpty(pkg)) {
            int p = getPid();
            String pkgName = getPackageName();
            if (p != Process.myPid() && pkgName.equals(pkg)) {
                result = false;
            }
        } else {
            result = false;
        }
        Log.i(TAG, "is valid : " + result + " Lpid : " + getPid() + " pid : " + Process.myPid() + " lp : " + getPackageName() + " p: " + pkg);
        put(pkg);
        return result;
    }

    public void clear() {
        pid = 0;
        packageName = "";
        sharedPreferences.edit().putString(APP_PKG, "")
                                .putInt(HOST_APP_PID, 0)
                                .apply();
    }

    public int getPid() {
        if (pid == 0) {
            pid = sharedPreferences.getInt(HOST_APP_PID, 0);
            packageName = sharedPreferences.getString(APP_PKG, "");
        }
        return pid;
    }

    public String getPackageName() {
        if (TextUtils.isEmpty(packageName)) {
            pid = sharedPreferences.getInt(HOST_APP_PID, 0);
            packageName = sharedPreferences.getString(APP_PKG, "");
        }
        return packageName;
    }

}
