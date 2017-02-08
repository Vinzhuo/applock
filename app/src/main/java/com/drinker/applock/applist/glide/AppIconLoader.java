package com.drinker.applock.applist.glide;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;

/**
 * Created by liuzhuolin on 2016/11/4.
 */

public class AppIconLoader implements ModelLoader<ApplicationInfo, Drawable> {
    private final Context context;

    public AppIconLoader(Context context) {
        this.context = context.getApplicationContext();
    }
    @Override
    public DataFetcher<Drawable> getResourceFetcher(ApplicationInfo model, int width, int height) {
        return new AppIconFetcher(context, model);
    }
}
