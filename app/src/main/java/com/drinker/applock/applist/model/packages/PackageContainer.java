package com.drinker.applock.applist.model.packages;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.LruCache;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.SparseArray;

import com.drinker.alock.lock.manager.LockHelper;
import com.drinker.alock.lock.manager.Persistence;
import com.drinker.applock.util.LockUtils;
import com.drinker.core.util.AppUtils;
import com.drinker.core.util.DeviceUtils;
import com.drinker.core.util.PinyinUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by yangqing.yq on 2015/8/15.
 */
public class PackageContainer implements Handler.Callback {

    private static final String TAG = "PackageContainer";

    private static final String THREAD_NAME_HELPER = "PackageHelperThread";

    protected static final int MSG_PACKAGE_CHANGED_FORCIBLY = 10;

    protected static final int MSG_PACKAGE_CHANGED = MSG_PACKAGE_CHANGED_FORCIBLY + 1;

    protected static final int MSG_PACKAGE_ADDED = MSG_PACKAGE_CHANGED + 1;

    protected static final int MSG_PACKAGE_REMOVED = MSG_PACKAGE_ADDED + 1;

    protected static final int MSG_PACKAGE_REPLACED = MSG_PACKAGE_REMOVED + 1;

    protected static final int MSG_PACKAGE_INFO_UPDATED = MSG_PACKAGE_REPLACED + 1;

    protected static final int MSG_PACKAGE_CONFIGURATION_CHANGED = MSG_PACKAGE_INFO_UPDATED + 1;

    public final static ExecutorService mExecutorService = Executors.newCachedThreadPool();

    private static String lang = DeviceUtils.getLanguage(AppUtils.getApp());

    protected static final int MSG_PACKAGE_SORT_FORCIBLY =
        MSG_PACKAGE_CONFIGURATION_CHANGED +1 ;


    private BroadcastReceiver packageReceiver;
    private BroadcastReceiver localeReceiver;

    private Handler handler;

    private ArrayList<PackageListener> listeners;

    private SparseArray<WeakReference<Handler>> handlerMap;

    private ArrayList<PackageData> packages;

    private final SparseArray<PackageData> packageMap;

    private PackageConfiguration configuration;

    private LruCache<String, Drawable> drawableLruCache;

    private static final int NO_POSITION = -1;
    private static final int CHANGE_TYPE_UPDATED = 0;
    private static final int CHANGE_TYPE_REMOVED = 1 + CHANGE_TYPE_UPDATED;
    private static final int CHANGE_TYPE_INSERTED = 1 + CHANGE_TYPE_REMOVED;

    private volatile boolean needRecommend = false;

    @IntDef({CHANGE_TYPE_UPDATED, CHANGE_TYPE_REMOVED, CHANGE_TYPE_INSERTED})
    private @interface ChangeTypeDef {
    }

    private static final class PackageComparator implements Comparator<PackageData> {

        private ArrayMap<String, Boolean> arrayMap;

        private PackageConfiguration configuration;

        PackageComparator(PackageConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public int compare(PackageData lhs, PackageData rhs) {
            // first compare locked
            boolean lhsLocked = lhs.isLocked();
            boolean rhsLocked = rhs.isLocked();
            if (lhsLocked != rhsLocked) {
                return lhsLocked ? -1 : 1;
            }
            if (lhsLocked) {
                if (lhs.getGetIntruderCount() == rhs.getGetIntruderCount()) {
                    if (lhs.getProtectedCount() != rhs.getProtectedCount()) {
                        return lhs.getProtectedCount() > rhs.getProtectedCount() ? -1: 1;
                    }
                } else {
                    return lhs.getGetIntruderCount() > rhs.getGetIntruderCount() ? -1 : 1;
                }
            }
            // second compare type
            int lhsType = lhs.getType();
            int rhsType = rhs.getType();
            if (lhsType != rhsType) {
                return lhsType == PackageType.RECOMMENDED ? -1 : 1;
            }

            if (lhsType == PackageType.RECOMMENDED) {
                PackageConfiguration configuration = this.configuration;
                PkgRecommend lhRec = configuration.getRecommend(lhs.getPackageName());
                PkgRecommend rhRec = configuration.getRecommend(rhs.getPackageName());
                int lhradia = (lhRec == null ? 0 : lhRec.getRatioWithLang(lang));
                int rhradia = (lhRec == null ? 0 : rhRec.getRatioWithLang(lang));
                if (lhradia != rhradia) {
                    return lhradia > rhradia ? -1 : 1;
                }
            }
            return compareLabel(lhs, rhs);
        }


        private int compareLabel(PackageData lhs, PackageData rhs) {
            ArrayMap<String, Boolean> arrayMap = this.arrayMap;
            if (arrayMap == null) {
                arrayMap = new ArrayMap<>();
                this.arrayMap = arrayMap;
            }

            String pinyinLeft = lhs.getPinyin();
            Boolean inAlphabetLeft = arrayMap.get(pinyinLeft);
            if (!TextUtils.isEmpty(pinyinLeft)) {
                if (inAlphabetLeft == null) {
                    inAlphabetLeft = pinyinLeft.substring(0, 1).toUpperCase().matches("[A-Z]");
                    arrayMap.put(pinyinLeft, inAlphabetLeft);
                }
            }

            String pinyinRight = rhs.getPinyin();
            Boolean inAlphabetRight = arrayMap.get(pinyinRight);
            if (!TextUtils.isEmpty(pinyinRight)) {
                if (inAlphabetRight == null) {
                    inAlphabetRight = pinyinRight.substring(0, 1).toUpperCase().matches("[A-Z]");
                    arrayMap.put(pinyinRight, inAlphabetRight);
                }
            }


            // then compare initial
            if (inAlphabetLeft != null && !inAlphabetLeft.equals(inAlphabetRight)) {
                return inAlphabetLeft ? -1 : 1;
            }

            // last compare label
            int result = 0;
            if (!TextUtils.isEmpty(pinyinLeft)) {
                result = pinyinLeft.compareToIgnoreCase(pinyinRight);
            }
            if (result == 0) {
                return lhs.getPackageName().compareTo(rhs.getPackageName());
            }

            return result;
        }
    }

    private interface Holder {
        PackageContainer INSTANCE = new PackageContainer();
    }

    @Override
    public boolean handleMessage(Message msg) {
        ArrayList<PackageData> packages = this.packages;
        switch (msg.what) {
            case PackageContainer.MSG_PACKAGE_SORT_FORCIBLY: {
                sort(packages);
                notifyOnChanged(packages);
                return true;
            }
            case PackageContainer.MSG_PACKAGE_CHANGED_FORCIBLY: {
                retrievePackages(packages);
                notifyOnChanged(packages);
                return true;
            }
            case PackageContainer.MSG_PACKAGE_CHANGED: {
                if (packages.size() == 0) {
                    retrievePackages(packages);
                }

                PackageListener listener = null;
                Object object = msg.obj;
                if (object instanceof WeakReference) {
                    object = ((WeakReference) msg.obj).get();
                    if (object instanceof PackageListener) {
                        listener = (PackageListener) object;
                    }
                }
                if (listener == null) {
                    notifyOnChanged(packages);
                } else {
                    notifyOnChanged(packages, listener);
                }
                return true;
            }
            case PackageContainer.MSG_PACKAGE_ADDED: {
                if (packages.size() == 0) {
                    retrievePackages(packages);
                } else if (msg.obj instanceof String) {
                    int position = insertPackage((String) msg.obj, packages);
                    if (position != NO_POSITION) {
                        PackageData data = packages.get(position);
                        notifyOnChanged(data, position, CHANGE_TYPE_INSERTED);
                        return true;
                    }
                }
                notifyOnChanged(packages);
                return true;
            }
            case PackageContainer.MSG_PACKAGE_REMOVED: {
                if (packages.size() == 0) {
                    retrievePackages(packages);
                } else if (msg.obj instanceof String) {
                    int position = checkPosition(packages, (String) msg.obj);
                    if (position != NO_POSITION) {
                        PackageData data = packages.remove(position);
                        notifyOnChanged(data, position, CHANGE_TYPE_REMOVED);
                        return true;
                    }
                }
                notifyOnChanged(packages);
                return true;
            }
            case PackageContainer.MSG_PACKAGE_REPLACED: {
                if (packages.size() == 0) {
                    retrievePackages(packages);
                } else if (msg.obj instanceof String) {
                    int position = updatePackage((String) msg.obj, packages);
                    if (position != NO_POSITION) {
                        PackageData data = packages.get(position);
                        notifyOnChanged(data, position, CHANGE_TYPE_UPDATED);
                        return true;
                    }
                    retrievePackages(packages);
                }
                notifyOnChanged(packages);
                return true;
            }
            case PackageContainer.MSG_PACKAGE_INFO_UPDATED: {
                if (packages.size() == 0) {
                    retrievePackages(packages);
                } else if (msg.obj instanceof String) {
                    int position = updatePackage((String) msg.obj, packages);
                    if (position != NO_POSITION) {
                        PackageData data = packages.get(position);
                        notifyOnChanged(data, position, CHANGE_TYPE_UPDATED);
                        return true;
                    }
                    retrievePackages(packages);
                }
                notifyOnChanged(packages);
                return true;
            }
            case PackageContainer.MSG_PACKAGE_CONFIGURATION_CHANGED: {
                configuration.fillRecommendData();
                TreeSet<PackageData> dataSet = new TreeSet<>(new PackageComparator(configuration));
                dataSet.addAll(packages);
                packages.clear();
                packages.addAll(dataSet);
                return true;
            }
        }

        return false;
    }

    private void sort(ArrayList<PackageData> packages) {
        TreeSet<PackageData> dataSet = new TreeSet<>(new PackageComparator(configuration));
        dataSet.addAll(packages);
        packages.clear();
        packages.addAll(dataSet);
    }


    private void markHighRecommend(List<PackageData> packages) {
        if (packages != null && packages.size() > 5) {
            int count = 0;
            for (PackageData packageData : packages) {
                /*if (count >= 5) {
                    break;
                }*/
                if (packageData.getType() == PackageType.RECOMMENDED) {
                    packageData.setHighRecommend(true);
                }
//                count++;
            }
        }
    }

    private int insertPackage(String packageName, ArrayList<PackageData> packages) {
        Context context = AppUtils.getApp();
        PackageManager manager = context.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = manager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (info == null) {
            return NO_POSITION;
        }

        Resources resources = context.getResources();
        Persistence storage = LockHelper.get().getPersistence();
        PackageData data = createPackageData(manager, resources, storage, info, true);

        synchronized (packageMap) {
            packageMap.put(data.getPackageName().hashCode(), data);
        }

        TreeSet<PackageData> dataSet = new TreeSet<>(new PackageComparator(configuration));
        dataSet.addAll(packages);
        dataSet.add(data);

        packages.clear();
        packages.addAll(dataSet);

        int position = checkPosition(packages, packageName);
        return position;
    }

    private int checkPosition(ArrayList<PackageData> packages, String packageName) {
        for (int i = packages.size() - 1; i >= 0; --i) {
            PackageData packageData = packages.get(i);
            if (packageData.getPackageName().equals(packageName)) {
                return i;
            }
        }
        return NO_POSITION;
    }

    private int updatePackage(String packageName, ArrayList<PackageData> packages) {
        Context context = AppUtils.getApp();
        PackageManager manager = context.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = manager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (info == null) {
            return NO_POSITION;
        }

        Resources resources = context.getResources();
        Persistence storage = LockHelper.get().getPersistence();
        PackageData data = createPackageData(manager, resources, storage, info, true);
        synchronized (packageMap) {
            packageMap.put(data.getPackageName().hashCode(), data);
        }
        int position = checkPosition(packages, packageName);
        if (position != NO_POSITION) {
            PackageData olddata = packages.get(position);
            data.setHighRecommend(olddata.isHighRecommend());
            packages.set(position, data);
        }
        return position;
    }

    private static final List<ResolveInfo> getIntentActivities(PackageManager manager) {
        List<ResolveInfo> list = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        try {
            list = manager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        } catch (Exception e) {
        } finally {
            return list;
        }
    }

    private void retrievePackages(ArrayList<PackageData> packages) {
        Context context = AppUtils.getApp();
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> list = getIntentActivities(manager);
        if (list.size() == 0) {
            return;
        }


        Persistence storage = LockHelper.get().getPersistence();

        if (list.size() > 100) {
            Set<PackageData> dataSet = new TreeSet<>(new PackageComparator(configuration));
            Set<PackageData> dataSet1 = new TreeSet<>(new PackageComparator(configuration));
            Set<PackageData> dataSet2 = new TreeSet<>(new PackageComparator(configuration));
            Set<PackageData> dataSet3 = new TreeSet<>(new PackageComparator(configuration));
            CountDownLatch latch = new CountDownLatch(3);
            RetrieveActivityWorker worker1 = new RetrieveActivityWorker(latch,0,list.size()/3,dataSet1,list,storage,context);
            RetrieveActivityWorker worker2 = new RetrieveActivityWorker(latch,(list.size()/3 + 1),list.size() *2/3,dataSet2,list,storage,context);
            RetrieveActivityWorker worker3 = new RetrieveActivityWorker(latch,(list.size() *2/3 +1),(list.size()-1),dataSet3,list,storage,context);
            if (!mExecutorService.isShutdown()) {
                mExecutorService.execute(worker1);
                mExecutorService.execute(worker2);
                mExecutorService.execute(worker3);
                try {
                    latch.await();
                } catch (Exception e) {
                }
            }

            dataSet.addAll(dataSet1);
            dataSet.addAll(dataSet2);
            dataSet.addAll(dataSet3);
            packages.clear();
            packages.addAll(dataSet);

        } else {
            TreeSet<PackageData> dataSet = new TreeSet<>(new PackageComparator(configuration));
            Resources resources = context.getResources();
            for (ResolveInfo info : list) {
                String packageName = info.activityInfo.applicationInfo.packageName;
                if (configuration.containsExclude(packageName)) {
                    continue;
                }

                PackageData data = createPackageData(manager, resources, storage, info
                        .activityInfo.applicationInfo, true);
                dataSet.add(data);

            }
            packages.clear();
            packages.addAll(dataSet);
        }
    }


    private PackageContainer() {
        packages = new ArrayList<>();
        packageMap = new SparseArray<>();
        configuration = new PackageConfiguration();
        needRecommend = !LockUtils.isLockSet();
    }

    public static PackageContainer get() {
        return Holder.INSTANCE;
    }

    /**
     *
     * @param what
     * @param object
     * @return
     */
    protected boolean trigger(int what, Object object) {
        Handler handler = this.handler;
        return handler != null && handler.sendMessage(handler.obtainMessage(what, object));
    }


    public void updatePackageData(String packageName) {
        trigger(MSG_PACKAGE_INFO_UPDATED, packageName);
    }

    public synchronized void updateRecommandData() {
        Handler helperHandler = retrieveHelperHandler();
        helperHandler.sendEmptyMessage(MSG_PACKAGE_CONFIGURATION_CHANGED);
        int what = PackageContainer.MSG_PACKAGE_CHANGED;
        Message message = helperHandler.obtainMessage(what);
        helperHandler.sendMessageDelayed(message, 0);
    }

    /**
     * @param listener we hold listener, caller should remove listener at time
     * @param handler  we are do not hold handler, caller should take care of it please,
     *                 null means will callback at our helper thread
     * @return true: add listener success, false: add listener false
     */
    public synchronized boolean addPackageListener(PackageListener listener, Handler handler) {
        if (listener == null) {
            return false;
        }

        long start = System.currentTimeMillis();
        ArrayList<PackageListener> listeners = this.listeners;
        if (listeners == null) {
            listeners = new ArrayList<>();
            this.listeners = listeners;
        }
        for (PackageListener l : listeners) {
            if (l == listener) {
                return false;
            }
        }

        listeners.add(listener);
        boolean filled = false;

        Handler helperHandler = retrieveHelperHandler();

        if (listeners.size() == 1) {
            registerReceiver();
            if (needRecommend) {
                helperHandler.sendEmptyMessage(MSG_PACKAGE_CONFIGURATION_CHANGED);
            }
        }

        if (handler != null && handler.getLooper() != null) {
            SparseArray<WeakReference<Handler>> map = handlerMap;
            if (map == null) {
                map = new SparseArray<>();
                handlerMap = map;
            }
            map.put(listener.hashCode(), new WeakReference<>(handler));
        }

        int what = PackageContainer.MSG_PACKAGE_CHANGED;
        if (filled) {
            what = PackageContainer.MSG_PACKAGE_CHANGED_FORCIBLY;
        }
        Message message = helperHandler.obtainMessage(what);
        message.obj = new WeakReference<>(listener);
        helperHandler.sendMessageDelayed(message, 0);
        long end = System.currentTimeMillis() - start;
        return true;
    }

    public synchronized boolean removePackageListener(PackageListener listener) {
        if (listener == null) {
            return false;
        }
        ArrayList<PackageListener> listeners = this.listeners;
        if (listeners == null) {
            return false;
        }

        boolean exist = false;
        for (PackageListener l : listeners) {
            if (l == listener) {
                exist = true;
                break;
            }
        }
        if (exist) {
            listeners.remove(listener);
            if (handlerMap != null) {
                handlerMap.remove(listener.hashCode());
            }
        }
        return true;
    }

    public String getPackageLabel(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return "";
        }
        PackageData data;
        synchronized (packageMap) {
            data = packageMap.get(packageName.hashCode());
        }
        if (data != null) {
            return data.getLabel();
        }
        Context context = AppUtils.getApp();
        PackageManager manager = context.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = manager.getApplicationInfo(packageName, 0);
        } catch (Exception e) {
        }
        if (info == null) {
            return "";
        }
        CharSequence charSequence = info.loadLabel(manager);
        if (charSequence != null) {
            return charSequence.toString();
        }
        return "";
    }

    public Pair<String, Drawable> getPackageInfo(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }

        PackageData data;
        synchronized (packageMap) {
            data = packageMap.get(packageName.hashCode());
        }
        if (data != null) {
            return Pair.create(data.getLabel(), getIcon(packageName));
        }

        Context context = AppUtils.getApp();
        PackageManager manager = context.getPackageManager();
        ApplicationInfo info = null;
        try {
            info = manager.getApplicationInfo(packageName, 0);
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        data = createPackageData(manager, context.getResources(), LockHelper.get().getPersistence(), info, true);
        synchronized (packageMap) {
             packageMap.put(packageName.hashCode(), data);
        }
        return Pair.create(data.getLabel(), getIcon(packageName));
    }

    private Drawable getIcon(String pkgName) {
        Drawable icon = getLruCached(pkgName);
        if (icon == null) {
            ApplicationInfo info = null;
            PackageManager manager = null;
            try {
                Context context = AppUtils.getApp();
                manager = context.getPackageManager();
                info = manager.getApplicationInfo(pkgName, 0);
                if (info == null) {
                    return null;
                }
                icon = info.loadIcon(manager);
                if (icon != null) {
                    putLruCached(info.packageName, icon);
                }
            } catch (Exception e) {
            }
        }
        return icon;
    }

    // 可能返回为null
    public Drawable getPackageIcon(String pkgName) {
        Drawable icon = getLruCached(pkgName);
//        if (icon == null) {
//            ApplicationInfo info = null;
//            PackageManager manager = null;
//            try {
//                Context context = PrivacyShieldApplication.get();
//                manager = context.getPackageManager();
//                info = manager.getApplicationInfo(pkgName, 0);
//            } catch (Exception e) {
//                if (DebugFlags.PACKAGE) {
//                    LogTool.w(TAG, e.toString());
//                }
//            }
//            if (info == null) {
//                return null;
//            }
//            icon = info.loadIcon(manager);
//            putLruCached(info.packageName, icon);
//        }
        return icon;
    }

    public Drawable getPackageIconImmediately(String pkgName) {
        Drawable icon = getLruCached(pkgName);
        if (icon == null) {
            ApplicationInfo info = null;
            PackageManager manager = null;
            try {
                Context context = AppUtils.getApp();
                manager = context.getPackageManager();
                info = manager.getApplicationInfo(pkgName, 0);
            } catch (Exception e) {

            }
            if (info == null) {
                return null;
            }
            icon = info.loadIcon(manager);
        }
        return icon;
    }

    private Drawable getPackageIcon(ApplicationInfo info) {
        Drawable icon = getLruCached(info.packageName);
        if (icon == null) {
            Context context = AppUtils.getApp();
            PackageManager manager = context.getPackageManager();
            icon = info.loadIcon(manager);
            putLruCached(info.packageName, icon);
        }
        return icon;
    }


    private void notifyOnChanged(List<PackageData> packages, PackageListener listener) {

        packages = new ArrayList<>(packages);
        if (needRecommend) {
//            markHighRecommend(packages);
        }
        boolean hasListener = false;
        Handler handler = null;
        synchronized (PackageContainer.this) {
            SparseArray<WeakReference<Handler>> map = handlerMap;
            if (map != null) {
                WeakReference<Handler> weak = map.get(listener.hashCode());
                if (weak != null) {
                    handler = weak.get();
                }
            }
            for (PackageListener l : this.listeners) {
                if (l.equals(listener)) {
                    hasListener = true;
                    break;
                }
            }
        }
        if (!hasListener) {
            return;
        }

        if (handler == null) {
            listener.onPackagesChanged(packages);
            return;
        }

        final PackageListener listenerFinal = listener;
        final List<PackageData> packagesFinal = packages;
        handler.post(new Runnable() {
            @Override
            public void run() {
                listenerFinal.onPackagesChanged(packagesFinal);
            }
        });
    }

    private void notifyOnChanged(List<PackageData> packages) {
        final List<PackageData> packagesFinal = new ArrayList<>(packages);
        Pair<PackageListener[], SparseArray<WeakReference<Handler>>> pair = getNotifyListenerInfo();
        if (pair == null) {
            return;
        }

        if (pair.second == null) {
            for (PackageListener listener : pair.first) {
                try {
                    listener.onPackagesChanged(packages);
                } catch (Exception e) {
                }
            }
            return;
        }

        for (PackageListener listener : pair.first) {
            WeakReference<Handler> weak = pair.second.get(listener.hashCode());
            Handler handler = null;
            if (weak != null) {
                handler = weak.get();
            }

            if (handler == null) {
                try {
                    listener.onPackagesChanged(packages);
                } catch (Exception e) {
                }
                continue;
            }

            final PackageListener listenerFinal = listener;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        listenerFinal.onPackagesChanged(packagesFinal);
                    } catch (Exception e) {
                    }
                }
            });

        }
    }

    private void notifyOnChanged(final PackageData data, final int position, @ChangeTypeDef final int changeType) {
        Pair<PackageListener[], SparseArray<WeakReference<Handler>>> pair = getNotifyListenerInfo();
        if (pair == null) {
            return;
        }
        if (pair.second == null) {
            for (PackageListener listener : pair.first) {
                notifyChange(listener, data, position, changeType);
            }
            return;
        }

        for (PackageListener listener : pair.first) {
            WeakReference<Handler> weak = pair.second.get(listener.hashCode());
            Handler handler = null;
            if (weak != null) {
                handler = weak.get();
            }

            if (handler == null) {
                notifyChange(listener, data, position, changeType);
                continue;
            }

            final PackageListener listenerFinal = listener;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    notifyChange(listenerFinal, data, position, changeType);
                }
            });

        }
    }

    private void notifyChange(PackageListener listener, PackageData data, int position, @ChangeTypeDef int changeType) {
        switch (changeType) {
            case CHANGE_TYPE_UPDATED:
                listener.onPackageUpdated(data, position);
                break;
            case CHANGE_TYPE_REMOVED:
                listener.onPackageRemoved(data, position);
                break;
            case CHANGE_TYPE_INSERTED:
                listener.onPackageInserted(data, position);
                break;
            default:
                break;
        }
    }

    private Pair<PackageListener[], SparseArray<WeakReference<Handler>>> getNotifyListenerInfo() {
        PackageListener[] listenerArray;
        SparseArray<WeakReference<Handler>> map;

        synchronized (PackageContainer.this) {
            ArrayList<PackageListener> listeners = this.listeners;
            if (listeners == null) {
                return null;
            }
            listenerArray = listeners.toArray(new PackageListener[listeners.size()]);

            map = handlerMap;
            if (map != null) {
                int size = map.size();
                SparseArray<WeakReference<Handler>> tempMap = new SparseArray<>(size);
                for (int i = 0; i < size; ++i) {
                    tempMap.put(map.keyAt(i), map.valueAt(i));
                }
                map = tempMap;
            }
        }
        return Pair.create(listenerArray, map);
    }


    private synchronized Handler retrieveHelperHandler() {
        Handler handler = this.handler;
        if (handler != null) {
            return handler;
        }

        long start = System.currentTimeMillis();
        HandlerThread thread = new HandlerThread(THREAD_NAME_HELPER);
        thread.start();

        handler = new Handler(thread.getLooper(), new PackageCallback(this));
        this.handler = handler;

        long end = System.currentTimeMillis() - start;
        return handler;
    }

    private synchronized boolean stopLooper() {
        Handler handler = this.handler;
        if (handler == null) {
            return false;
        }
        long start = System.currentTimeMillis();
        handler.removeCallbacksAndMessages(null);
        handler.getLooper().quit();
        this.handler = null;

        long end = System.currentTimeMillis() - start;
        return true;
    }

    private synchronized void registerReceiver() {
        if (packageReceiver != null) {
            return;
        }
        long start = System.currentTimeMillis();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme(PackageReceiver.INTENT_DATA_SCHEME);
        packageReceiver = new PackageReceiver(this);
        Context applicationContext = AppUtils.getApp();
        applicationContext.registerReceiver(packageReceiver, filter);
        localeReceiver = new PackageReceiver(this);
        applicationContext.registerReceiver(localeReceiver, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));

        long end = System.currentTimeMillis() - start;

    }

    private synchronized void unregisterReceiver() {
        if (packageReceiver == null) {
            return;
        }
        Context applicationContext = AppUtils.getApp();
        applicationContext.unregisterReceiver(packageReceiver);
        applicationContext.unregisterReceiver(localeReceiver);
        packageReceiver = null;
        localeReceiver = null;
    }

    synchronized void putLruCached(String pkgName, Drawable drawable) {
        if (pkgName == null || drawable == null) {
            return;
        }
        if (drawableLruCache == null) {
            initDrawableLruCache();
        }
        drawableLruCache.put(pkgName, drawable);
    }

    synchronized Drawable getLruCached(String pkgName) {
        if (pkgName == null) {
            return null;
        }
        if (drawableLruCache == null) {
            initDrawableLruCache();
        }
        return drawableLruCache.get(pkgName);
    }

    private void initDrawableLruCache() {
        ActivityManager manager = (ActivityManager) AppUtils.getApp().getSystemService(Context.ACTIVITY_SERVICE);
        int max = manager.getMemoryClass() * 1024 * 1024;
        max = max / 9;
        drawableLruCache = new LruCache<>(max);
    }

    private PackageData createPackageData(PackageManager manager, Resources resources, Persistence storage,
                                          ApplicationInfo info, boolean needIcon) {
        String label = info.loadLabel(manager).toString();
        String name = info.packageName;
        int type = PackageType.NORMAL;
        if (configuration.containsRecommendWithValidData(name)) {
            type = PackageType.RECOMMENDED;
        }

        boolean locked = storage.isLocked(name);
        PackageData data;
        if (type == PackageType.RECOMMENDED) {
            data = new PackageData(type, locked, name, configuration.getRecommend
                (name));
        } else {
            data = new PackageData(type, locked, name);
        }

        data.setLabel(label);
        String pinyin = label;
        if (PinyinUtils.isContainsChinese((label))) {
            pinyin = PinyinUtils.getPinyin(label);
        }
        data.setPinyin(pinyin);
        data.setApplicationInfo(info);
        return data;
    }

    public synchronized void clearCache() {
        if (drawableLruCache != null) {
            drawableLruCache.evictAll();
        }
    }

    public void packageSort() {
        trigger(MSG_PACKAGE_SORT_FORCIBLY, "");
    }

    public PackageConfiguration getConfiguration() {
        return configuration;
    }

    public void pullRecommand() {
        PackageContainer.get().updateRecommandData();
    }


    private class RetrieveActivityWorker implements Runnable {

        private CountDownLatch downLatch;
        Set<PackageData> dataSet;
        List<ResolveInfo> list;
        Persistence storage;
        Context ctx;
        Resources resources;
        PackageManager manager;
        private int start;
        private int end;
        int recommendedCount = 0;
        private static final int ICON_LIMIT_MAX = 10;
        int iconCount = 0;
        @Override
        public void run() {
            for (int pos = start; pos <= end; pos++) {
                ResolveInfo info = list.get(pos);
                String packageName = info.activityInfo.applicationInfo.packageName;
                if (configuration.containsExclude(packageName)) {
                    continue;
                }
                PackageData data;
                if (iconCount > ICON_LIMIT_MAX) {
                    data = createPackageData(manager, resources, storage, info
                            .activityInfo.applicationInfo, false);
                } else {
                    data = createPackageData(manager, resources, storage, info
                            .activityInfo.applicationInfo, true);
                    iconCount++;
                }

                dataSet.add(data);
                if (data.getType() == PackageType.RECOMMENDED) {
                    recommendedCount ++;
                }
            }
            this.downLatch.countDown();
        }
        public RetrieveActivityWorker(CountDownLatch downLatch, int start, int end, Set<PackageData> dataSet, List<ResolveInfo> list, Persistence storage, Context ctx ) {
            this.downLatch = downLatch;
            this.start = start;
            this.end = end;
            this.dataSet = dataSet;
            this.list = list;
            this.storage = storage;
            this.ctx =ctx;
            manager = ctx.getPackageManager();
            resources = ctx.getResources();
        }
    }
}
