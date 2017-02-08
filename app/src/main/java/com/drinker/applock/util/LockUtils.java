package com.drinker.applock.util;

import android.text.TextUtils;

import com.drinker.applock.password.util.PasswordUtil;
import com.drinker.core.util.AppUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhuolin on 16/4/12.
 */
public class LockUtils {

    private static AtomicBoolean isLockSet = new AtomicBoolean();

    static {
        String currentPassword = PasswordUtil.getPin(AppUtils.getApp());
        if(!TextUtils.isEmpty(currentPassword)) {
            isLockSet.set(true);
        } else {
            isLockSet.set(false);
        }
    }

    public static void setLock() {
        isLockSet.set(true);
    }

    public static boolean isLockSet() {
        return isLockSet.get();
    }

}
