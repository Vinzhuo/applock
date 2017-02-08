package com.drinker.applock;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.drinker.alock.lock.manager.LockHelper;
import com.drinker.applock.password.view.CompareActivity;
import com.r0adkll.slidr.model.SlidrListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class BaseActivity extends AppCompatActivity implements SlidrListener {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private boolean onCreated = false;

    private static int foregroundCount = 0;
    private  Handler mainHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //防止 fragment getActivity 为null
            savedInstanceState.remove("android:support:fragments");
        }
        try {
            super.onCreate(savedInstanceState);
        } catch (Exception e) {
            finish();
            return;
        }
        if (!onCreated) {
            ++foregroundCount;
        }
        if (foregroundCount == 1) {
            notifySelfForeground();
        }
        beforeInitView();
        initView();
        initListener();
        initData();
//        Slidr.attach(this, SlidrUtils.getConfig(this, this));
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (onCreated) {
            ++foregroundCount;
            if (foregroundCount == 1) {
                notifySelfForeground();
            }
        } else {
            onCreated = true;
        }
    }

    private void lockSelf() {
        CompareActivity.start(this, getPackageName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (onCreated) {
            --foregroundCount;
        }
        //去后台
        if (foregroundCount == 0) {
            notifySelfBackground();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!onCreated) {
            --foregroundCount;
        }
        onCreated = false;
    }


    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
            invokeFragmentManagerNoteStateNotSaved();
        } catch (Exception e) {
        }
    }

    private void invokeFragmentManagerNoteStateNotSaved() {
        /**
         * For post-Honeycomb devices
         */
        if (Build.VERSION.SDK_INT < 11) {
            return;
        }
        try {
            Class cls = getClass();
            do {
                cls = cls.getSuperclass();
            } while (!"Activity".equals(cls.getSimpleName()));
            Field fragmentMgrField = cls.getDeclaredField("mFragments");
            fragmentMgrField.setAccessible(true);

            Object fragmentMgr = fragmentMgrField.get(this);
            cls = fragmentMgr.getClass();

            Method noteStateNotSavedMethod = cls.getDeclaredMethod("noteStateNotSaved", new Class[] {});
            noteStateNotSavedMethod.invoke(fragmentMgr, new Object[]{});
        } catch (Exception ex) {
        }
    }


    final private void notifySelfBackground() {
        LockHelper.get().startLock();
    }

    final private void notifySelfForeground() {
        LockHelper.get().stopLock();
        if (isLockAtForeground(getIntent())) {
            lockSelf();
        }
    }/**
     * if you want to lock this activity, when activity go to foreground
     * use the method to control to lock or not
     * @param intent
     * @return
     */
    protected abstract boolean isLockAtForeground(Intent intent);

    public abstract void beforeInitView();

    protected abstract void initView();

    protected abstract void initListener();

    protected abstract void initData();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected Handler getMainHandler() {
        if (this.mainHandler == null) {
            this.mainHandler = new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

}
