package com.drinker.alock.lock.manager;


import android.content.Context;
import android.text.TextUtils;


import com.drinker.alock.util.log.Log;
import com.drinker.core.util.DeviceUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by zhuolin on 16/4/25.
 */
public class LockAppConfiguration {

    private static final String TAG = LockAppConfiguration.class.getSimpleName();
    private final Context context;

    LockAppConfiguration(Context cxt) {
        context = cxt;
    }

    private String [] systemChoiceDialogs = new String[] {
            "system", "android", "com.huawei.android.internal.app"
    };

    private String [] systemPhone = new String[] {
            "com.android.phone", "com.android.incallui"
    };

    // 能拔打电话的应用包名
    private String [] systemDialer = new String[] {
            "com.android.dialer", "com.android.contacts"
    };

    private String[] specialActivitys = new String[] {
            "com.whatsapp.OverlayAlert"
    };

    public List<String> getSystemDialers() {
        return Arrays.asList(systemDialer);
    }

    private boolean isSystemChoiceDialog(String pkg) {
        if (!TextUtils.isEmpty(pkg)) {
            for (String dialog : systemChoiceDialogs) {
                if (pkg.equals(dialog)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSystemPhone(String pkg) {
        if (!TextUtils.isEmpty(pkg)) {
            for (String phone : systemPhone) {
                if (pkg.equals(phone)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValidPackageName(String pkg) {
        boolean result = true;
        if (isSystemChoiceDialog(pkg)) {
            result = false;
        } else if (isSystemPhone(pkg) && !DeviceUtils.hasSimCard(context)) {
            result = false;
        }
        Log.e(TAG, "valid : " + result);
        return result;
    }

    public boolean isValidActivity(String activityName) {
        if (!TextUtils.isEmpty(activityName)) {
            for (String activity : specialActivitys) {
                if (activityName.equals(activity)) {
                    return false;
                }
            }
        }
        return true;
    }

}
