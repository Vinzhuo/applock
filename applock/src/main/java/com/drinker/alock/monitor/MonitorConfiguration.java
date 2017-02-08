package com.drinker.alock.monitor;

import android.text.TextUtils;

/**
 * Created by zhuolin on 16/3/31.
 */
public class MonitorConfiguration {
   private String[] specialModels = new String[] {
           "htc HTC 802W", "Huawei HUAWEI C8816"
   };
    private boolean modelContains(String m) {
        for (String model : specialModels) {
            if (m.equalsIgnoreCase(model)) {
                return true;
            }
        }
        return false;
    }
    public boolean isSpeical(String model) {
        if (!TextUtils.isEmpty(model) && modelContains(model)) {
            return true;
        }
        return false;
    }
}
