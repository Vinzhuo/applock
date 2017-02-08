package com.drinker.applock.applist.glide;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.drinker.alock.util.log.Log;

/**
 * Created by liuzhuolin on 2016/11/4.
 */

public class AppIconFetcher implements DataFetcher<Drawable> {

    private final ApplicationInfo source;
    private final Context context;
    private final PackageManager packageManager;

    AppIconFetcher(Context context, ApplicationInfo source) {
        this.context = context;
        this.source = source;
        this.packageManager = context.getPackageManager();
    }

    @Override
    public Drawable loadData(Priority priority) throws Exception {
        return packageManager.getApplicationIcon(source.packageName);
    }

    @Override
    public void cleanup() {

    }

    @Override
    public String getId() {
        return source.packageName;
    }

    @Override
    public void cancel() {

    }
}
