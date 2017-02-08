package com.drinker.alock.lock.manager;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuolin on 15-8-22.
 */
final class AppRecord {

    private static final int TOP_APP_PATH_MAXSIZE = 4;
    private List<PkgRecord> topAppPath = new ArrayList<>(TOP_APP_PATH_MAXSIZE);

    PkgRecord get(int location) {
        if (location < 0 || location >= topAppPath.size()) {
            return null;
        }
        return topAppPath.get(location);
    }

    void push(PkgRecord object) {
        if (object == null || TextUtils.isEmpty(object.getPkgname())) {
            return;
        }
        PkgRecord top = get(0);
        if (top != null && object.getPkgname().equals(top.getPkgname())) {
            return;
        }
        int size = topAppPath.size();
        if (size >= TOP_APP_PATH_MAXSIZE) {
            topAppPath.remove(size - 1);
        }
        topAppPath.add(0, object);
    }

    PkgRecord peek() {
        int size = topAppPath.size();
        if (size <= 0) {
            return null;
        }
        return topAppPath.get(0);
    }

    static class PkgRecord {
        private String pkgname = "";
        private boolean needLock = true;
        public void setPkgname(String name) {
            pkgname = name;
        }
        public String getPkgname() {
            return pkgname;
        }
        public void setNeedLock(boolean lock) {
            needLock = lock;
        }
        public boolean isNeedLock() {
            return needLock;
        }
    }

}
