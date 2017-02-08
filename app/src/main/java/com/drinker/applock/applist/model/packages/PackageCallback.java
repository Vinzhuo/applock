package com.drinker.applock.applist.model.packages;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;


final class PackageCallback implements Handler.Callback {

    private WeakReference<PackageContainer> reference;

    protected PackageCallback(PackageContainer container) {
        reference = new WeakReference<>(container);
    }

    @Override
    public boolean handleMessage(Message msg) {
        PackageContainer container = reference.get();
        return container != null && container.handleMessage(msg);
    }

}
