package com.drinker.applock;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.drinker.alock.lock.manager.ILockListener;
import com.drinker.alock.lock.manager.LockHelper;
import com.drinker.alock.lock.manager.LockedApp;
import com.drinker.applock.password.view.CompareActivity;
import com.drinker.applock.util.ToastUtils;
import com.drinker.core.util.AppUtils;
import com.drinker.core.util.DeviceUtils;
import com.drinker.core.util.IntentUtils;

import java.util.HashMap;


public class LockService extends Service implements ILockListener, Handler.Callback {

    public static final String START_ACTION = "android.intent.action.STARTMONITOR";
    public static final String STOP_ACTION = "android.intent.action.STOPMONITOR";
    public static final String KEEPALIVE_ACTION = "android.intent.action.KEEPALIVE";
    public static final String NOTI_INSTALL_ACTION = "android.intent.action.NOTI_INSTALL_ACTION";
    public static final String NOTI_SECURITY_QUESTION_ACTION = "android.intent.action.NOTI_SECURITY_QUESTION_ACTION";
    private static final String TAG = LockService.class.getSimpleName();
    private Handler handler = null;
    private ScreenReceiver screenReceiver = new ScreenReceiver();
    private long lastUpdateLockedAppTime = 0;
    private static LockService service = null;
    private NotificationManager notificationManager;
    private HashMap<String, String> reportMap = new HashMap<>(1);
    private String deviceid = DeviceUtils.getDeviceToken(AppUtils.getApp());
    private static final int APP_PRESENT_MESSAGE = 1;
    private static final int SHOW_LOCK_TIP_MESSAGE = APP_PRESENT_MESSAGE + 1;

    // 用户上报
    private long reportTime = 0;
    private HashMap<String, String> appReportMap = new HashMap(1);
    private String deviceId;
    private static final int DAY = 60*60*24*1000;

    @Override
    public void onLockedAppPresent(final String packageName, String activityName) {
        Message message = new Message();
        message.what = APP_PRESENT_MESSAGE;
        message.obj = packageName;
        handler.sendMessage(message);
    }

    @Override
    public void onTopAppChange(String packageName, String activityName) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }
    }


    @Override
    public boolean handleMessage(Message msg) {
        int what = msg.what;
        switch (what) {
            case APP_PRESENT_MESSAGE:
                String packageName = (String) msg.obj;
                if (LockHelper.get().isNeedNotify(packageName)) {
                    startActivityForLockApp(packageName);
                } else {
                    if (!this.getPackageName().equals(packageName)) {
                        LockHelper.get().setLockedAppStatus(packageName, LockedApp.PRESENTED);
                        ToastUtils.showShortToast(this, R.mipmap.ic_launcher, R.string.lock_toast);
                    }
                }
                break;
        }
        return true;
    }

    private void startActivityForLockApp(String pkgname) {
        try {
            CompareActivity.start(this, pkgname);
        } catch (Exception e) {
        }
    }

    private class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!IntentUtils.isValid(intent)) {
                return;
            }
            try{
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    LockHelper.get().stopLock();
                    LockHelper.get().resetAppNotify();
                } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    LockHelper.get().startLock();
                    LockHelper.get().resetAppsStatus("");
                    LockHelper.get().onChanged();
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    LockHelper.get().startLock();
                }
            }catch(Exception ex){
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        deviceId = DeviceUtils.getDeviceToken(this) + " : " + Build.VERSION.SDK_INT;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);

        this.registerReceiver(screenReceiver, intentFilter);
        handler = new Handler(Looper.getMainLooper(), this);
        notificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        LockHelper.get().addListener(this);
        LockHelper.get().startLock();
        service = this;
        keepForground();
    }

    private void keepForground() {
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            Intent innerIntent = new Intent(this, NotifyService.class);
            startService(innerIntent);
        }
        NotifyService.startForeground(this); //将服务优先级设置和前台一样，提高服务的存活率
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    public void onDestroy() {
        super.onDestroy();
        LockHelper.get().removeListener(this);
        this.unregisterReceiver(screenReceiver);
        service = null;
    }

    /**
     * 保活内部服务
     */
    public static class NotifyService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(this);
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        /**
         * setForeground是错误的写法，这个才是正确的开启前台service
         * 的方法
         */
        @SuppressWarnings("deprecation")
        private static void startForeground(Service service) {
            if (service != null) {
                Notification note = new Notification(0, null, System.currentTimeMillis());
                // 这个会造成状态栏显示有信的通话记录
                note.flags = Notification.FLAG_ONGOING_EVENT;
                note.flags |= Notification.FLAG_NO_CLEAR;
                note.flags |= Notification.FLAG_FOREGROUND_SERVICE;
                service.startForeground(43, note); // 1 42
            }
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }

}