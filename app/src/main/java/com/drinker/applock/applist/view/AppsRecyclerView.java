package com.drinker.applock.applist.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by liuzhuolin on 2016/10/24.
 */

public class AppsRecyclerView extends RecyclerView {
    private double scale = 1f;

    public AppsRecyclerView(Context context) {
        super(context);
    }

    public AppsRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AppsRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setflingScale(double scale){
        this.scale = scale;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        velocityY *= scale;
        return super.fling(velocityX, velocityY);
    }

}
