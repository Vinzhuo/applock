package com.drinker.applock;

import android.app.Application;
import android.content.Intent;

import com.drinker.alock.lock.manager.LockHelper;
import com.drinker.watchdog.Configurations;
import com.drinker.watchdog.WatchDogApp;

/**
 * Created by liuzhuolin on 2016/11/4.
 */

public class DrinkApplication extends WatchDogApp {
    @Override
    public void onCreate() {
        LockHelper.get().init(this, null);
        Intent intent = new Intent(this, LockService.class);
        startService(intent);
    }

    @Override
    protected Configurations.Configuration onWatchDogConfig() {
        return new Configurations.Configuration(getPackageName(), LockService.class, LockReceiver.class);
    }
}
