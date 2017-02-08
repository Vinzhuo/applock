package com.drinker.alock.lock.annotations;


import android.support.annotation.IntDef;

import com.drinker.alock.lock.manager.LockedApp;


/**
 * Created by zhuolin on 15-8-19.
 */
@IntDef({LockedApp.BACKGROUND, LockedApp.PRESENTED, LockedApp.LOCKING, LockedApp.UNLOCK})
public @interface LockAppStatusDef {
}
