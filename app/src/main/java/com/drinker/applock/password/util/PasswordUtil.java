package com.drinker.applock.password.util;

import android.content.Context;

import com.drinker.applock.password.Contants;


/**
 */
public class PasswordUtil {

    /**
     * 获取设置过的密码
     */
    public static String getPin(Context context) {
        String password = ConfigUtil.getInstance(context).getString(Contants.PASS_KEY);
        return password;
    }
}
