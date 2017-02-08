package com.drinker.applock.applist.view;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.drinker.alock.lock.manager.LockHelper;
import com.drinker.applock.R;
import com.drinker.applock.applist.glide.AppIconDecoder;
import com.drinker.applock.applist.glide.AppIconLoader;
import com.drinker.applock.applist.model.packages.PackageData;
import com.drinker.core.util.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuzhuolin on 2016/11/4.
 */

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.AppViewHolder> {

    private final Context context;
    private final LayoutInflater inflater;
    private final GenericRequestBuilder<ApplicationInfo, Drawable, Drawable, Drawable> glideRequestBuilder;
    private final AppIconDecoder drawableDecoder;
    private final Drawable lockIcon;
    private final Drawable unlockIcon;
    private List<PackageData> packageDataList = new ArrayList<>();

    public AppsAdapter() {
        this.context = AppUtils.getApp().getApplicationContext();
        this.inflater = LayoutInflater.from(context);
        this.drawableDecoder = new AppIconDecoder();
        this.lockIcon = context.getResources().getDrawable(R.drawable.icon_lock_on);
        this.unlockIcon = context.getResources().getDrawable(R.drawable.icon_lock_off);
        this.glideRequestBuilder = Glide.with(context)
                .using(new AppIconLoader(context), Drawable.class)
                .from(ApplicationInfo.class)
                .as(Drawable.class)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .decoder(drawableDecoder);
    }

    public void addAll(List<PackageData> datas) {
        packageDataList.clear();
        packageDataList.addAll(datas);
        notifyDataSetChanged();
    }


    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        PackageData app = packageDataList.get(position);
        holder.setPosition(position);
        holder.appName.setText(app.getLabel());
        glideRequestBuilder.load(app.getApplicationInfo()).into(holder.appIcon);
        if (app.isLocked()) {
            holder.lockIcon.setImageDrawable(lockIcon);
        } else {
            holder.lockIcon.setImageDrawable(unlockIcon);
        }
    }


    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(inflater.inflate(R.layout.list_item_layout, parent, false));
    }

    PackageData getItem(int position) {
        return packageDataList.get(position);
    }

    @Override
    public int getItemCount() {
        return packageDataList.size();
    }

    class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ImageView appIcon;
        private final TextView appName;
        private final ImageView lockIcon;


        public void setPosition(int position) {
            this.position = position;
        }
        private int position;

        public AppViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
            this.appName = (TextView) itemView.findViewById(R.id.app_name);
            this.lockIcon = (ImageView) itemView.findViewById(R.id.item_lock);
        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_layout:
                    PackageData p = getItem(position);
                    if (p.isLocked()) {
                        lockIcon.setImageDrawable(unlockIcon);
                        p.setLocked(false);
                        LockHelper.get().unLock(p.getPackageName());
                    } else {
                        lockIcon.setImageDrawable(AppsAdapter.this.lockIcon);
                        p.setLocked(true);
                        LockHelper.get().lock(p.getPackageName());
                    }
                    break;
            }
        }
    }

}
