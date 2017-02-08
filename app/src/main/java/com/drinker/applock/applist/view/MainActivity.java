package com.drinker.applock.applist.view;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;

import com.drinker.applock.BaseActivity;
import com.drinker.applock.R;
import com.drinker.applock.applist.model.packages.PackageContainer;
import com.drinker.applock.applist.model.packages.PackageData;
import com.drinker.applock.applist.model.packages.PackageListener;

import java.util.List;

public class MainActivity extends BaseActivity implements PackageListener {

    private AppsRecyclerView recycleView;
    private AppsAdapter appAdapter;

    @Override
    protected boolean isLockAtForeground(Intent intent) {
        return true;
    }

    @Override
    public void beforeInitView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initView() {
        this.recycleView = (AppsRecyclerView) findViewById(R.id.applist_recyclerview);
        this.recycleView.setLayoutManager(new LinearLayoutManager(this));
        this.recycleView.addItemDecoration(new DividerDecoration(this.getResources()));
        this.appAdapter = new AppsAdapter();
        this.recycleView.setflingScale(1.5);
        this.recycleView.setAdapter(this.appAdapter);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        PackageContainer.get().addPackageListener(this, getMainHandler());
    }


    @Override
    public void onPackagesChanged(List<PackageData> packages) {
        appAdapter.addAll(packages);
    }

    @Override
    public void onPackageUpdated(PackageData data, int position) {

    }

    @Override
    public void onPackageRemoved(PackageData data, int position) {

    }

    @Override
    public void onPackageInserted(PackageData data, int position) {

    }

    @Override
    public void onSlideStateChanged(int state) {

    }

    @Override
    public void onSlideChange(float percent) {

    }

    @Override
    public void onSlideOpened() {

    }

    @Override
    public void onSlideClosed() {

    }
}
