package com.drinker.alock.monitor;

/**
 * Created by zhuolin on 15-7-23.
 */
public interface IActivityMonitorListener {
    public void onChanged(String packageName, String activityName);
}
