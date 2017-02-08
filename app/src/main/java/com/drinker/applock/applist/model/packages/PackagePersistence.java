package com.drinker.applock.applist.model.packages;

import android.content.Context;

import com.drinker.alock.lock.manager.Persistence;



public class PackagePersistence extends Persistence {

    public PackagePersistence(Context context) {
        super(context);
    }

    protected synchronized final String decode(Context context, String stored) {
        return "";
    }

    protected synchronized final String encode(Context context, String source) {

        return "";
    }
}

