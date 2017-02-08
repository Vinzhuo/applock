package com.drinker.alock.monitor.activitymonitor;

/**
 * Created by zhuolin on 15-7-23.
 */
public interface IActivityMonitor {
    public void start();
    public void stop();
    public void setListener(IWatcher watcher);
}
