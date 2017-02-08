package com.drinker.alock.lock.manager;

/**
 * Created by zhuolin on 15-7-28.
 */
public class LockedApp {

    public static final int BACKGROUND = 1;

    public static final int PRESENTED = BACKGROUND + 1;

    public static final int LOCKING = PRESENTED + 1;

    public static final int UNLOCK = LOCKING + 1;

    private String packageName = "";

    private int status = BACKGROUND;

    private boolean needNotify = true;

    private boolean needLock = true;

    public void setStatus(int stat) {
        status = stat;
    }

    public int getStatus() {
        return status;
    }

    public void setPackageName(String pkg) {
        packageName = pkg;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setLock(boolean need) {
        needLock = need;
    }

    public boolean needLock() {
        return needLock;
    }

    public void setNotify(boolean need) {
        needNotify = need;
    }

    public boolean needNotify() {
        return needNotify;
    }

}
