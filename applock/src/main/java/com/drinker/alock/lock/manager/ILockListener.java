package com.drinker.alock.lock.manager;

/**
 * Created by zhuolin on 15-8-17.
 */
public interface ILockListener {
//    public void onActivityStart(String packageName, String activityName);
    void onLockedAppPresent(String packageName, String activityName);
    void onTopAppChange(String packageName, String activityName);
}
