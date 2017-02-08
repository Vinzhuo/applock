/*
 * 深圳市有信网络技术有限公司
 * Copyright (c) 2016 All Rights Reserved.
 */

package com.drinker.watchdog;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.drinker.watchdog.strategy.ExcelpWatchStrategy;
import com.drinker.watchdog.strategy.FileWatchStrategy;
import com.drinker.watchdog.strategy.StrongExcelWatchStrategy;
import com.drinker.watchdog.strategy.StrongFileWatchStrategy;
import com.drinker.watchdog.strategy.WatchDogStrategy;
import com.drinker.watchdog.strategy.boot.BinderBroadcastPolicy;
import com.drinker.watchdog.strategy.boot.BinderServicePolicy;


public class WatchDog {

    public static final String TAG = "WatchDog";
    
    private static final String WATCH_DOG_PREFRENCE = "watchdog_preference";

    private static final String WATCH_DOG_SUPPORT_KEY = "watchdog_support";

    private static WatchDogStrategy sStrategy;

    private static Object sLock = new Object();

    public static WatchDogStrategy fetch() {
        synchronized (sLock) {
            if (sStrategy != null) {
                return sStrategy;
            }
            final int sdk = Build.VERSION.SDK_INT;
            switch (sdk) {
                case 24: //7.0
                case 23: // 6.0
                    if (Build.BRAND.toLowerCase().startsWith("honor")) {
                        sStrategy = new FileWatchStrategy(new BinderServicePolicy());
                    }
                    else if ("xiaomi".equals(Build.BRAND.toLowerCase())) {
                        sStrategy = new StrongFileWatchStrategy(new BinderServicePolicy());
                    }
                    else {
                        sStrategy = new FileWatchStrategy(new BinderBroadcastPolicy());
                    }
                    break;
                case 22: // 5.1
                    if (Build.MODEL.toLowerCase().startsWith("mx4") ||
                            Build.MODEL.toLowerCase().startsWith("oppo r9")) {
                        sStrategy = new StrongFileWatchStrategy(new BinderServicePolicy());
                    } else {
                        sStrategy = new FileWatchStrategy(new BinderServicePolicy());
                    }
                    break;
                case 21: // 5.0
                    if(Build.MODEL != null && Build.MODEL.toLowerCase().startsWith("redmi note")){
                        /**
                         * xiaomi redmi note 3 5.0.2
                         */
                        sStrategy = new FileWatchStrategy(new BinderBroadcastPolicy());
                    }
                    else if ("xiaomi".equals(Build.BRAND.toLowerCase())) {
                        sStrategy = new StrongFileWatchStrategy(new BinderServicePolicy());
                    }
                    else {
                        sStrategy = new FileWatchStrategy(new BinderServicePolicy());
                    }
                    break;
                default: // <5.0

                    if (Build.BRAND.toLowerCase().startsWith("honor")) {
                        sStrategy = new StrongFileWatchStrategy(new BinderServicePolicy());
                    }
                    else if ("oppo".equals(Build.BRAND.toLowerCase()) ||
                            "vivo".equals(Build.BRAND.toLowerCase()) ||
                            "xiaomi".equals(Build.BRAND.toLowerCase())) {
                        sStrategy = new StrongExcelWatchStrategy(new BinderServicePolicy());
                    }
                    else {
                        sStrategy = new ExcelpWatchStrategy(new BinderServicePolicy());
                    }
            }
            return sStrategy;
        }
    }

    /**
     * 注意有多进程调用
     * @param context
     * @return
     */
    public static boolean isSuportWatchDog(Context context) {
        SharedPreferences preference = context.getSharedPreferences(WATCH_DOG_PREFRENCE,
                Context.MODE_PRIVATE);
        return preference.getBoolean(WATCH_DOG_SUPPORT_KEY, true);
    }

    /**
     * 注意有多进程调用
     * @param context
     * @param support
     * @return
     */
    public static boolean setSupportWatchDog(Context context, boolean support) {
        SharedPreferences preference = context.getSharedPreferences(WATCH_DOG_PREFRENCE,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();
        editor.putBoolean(WATCH_DOG_SUPPORT_KEY, support);
        return editor.commit();
    }

}
