package com.drinker.watchdog;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class WatchDogService extends Service {
    public WatchDogService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        keepForground();
    }
    private void keepForground() {
        if (android.os.Build.VERSION.SDK_INT >= 18) {
            Intent innerIntent = new Intent(this, NotifyService.class);
            startService(innerIntent);
        }
        NotifyService.startForeground(this); //将服务优先级设置和前台一样，提高服务的存活率
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Configurations.startWorkService(this);
        return  super.onStartCommand(intent, flags, startId);
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
