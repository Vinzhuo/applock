package com.drinker.core.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by zhuolin on 15-12-4.
 */
public class StorageUtils {
    private static final String TAG = StorageUtils.class.getSimpleName();
    private final static int    minSpaceMB = 10;
    private static String[]volumePaths = null;
    public static String[] getVolumePaths(Context context) {
        if (volumePaths != null && volumePaths.length > 0) {
            return volumePaths;
        }
        try {
            StorageManager sm = (StorageManager) context
                    .getSystemService(Activity.STORAGE_SERVICE);
            Method getVolumePaths = sm.getClass().getMethod("getVolumePaths");
            getVolumePaths.setAccessible(true);
            volumePaths = (String[]) getVolumePaths.invoke(sm);
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        } catch (Exception e) {
        }
        return volumePaths;
    }

    public static String getInternalSdcardPath() {
        String path = "";
        String status = "";

        try {
           status =  Environment.getExternalStorageState();
        } catch (Exception e) {
        }
        if (Environment.MEDIA_MOUNTED.equals(status)) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();

        }
        return path;
    }

    public static boolean hasSdcard() {
        boolean has_sdcard = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED);
        return has_sdcard;
    }

    public static boolean isExternalSpaceInSufficient() {
        try {
            String path = getInternalSdcardPath();
            if (path.equals("")) {
                return false;
            }
            StatFs statFs = new StatFs(path);
            long availableSpace = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableSpace = statFs.getAvailableBytes()/ (1024 * 1024);
            } else {
                long blocSize = statFs.getBlockSize();
                long availaBlock = statFs.getAvailableBlocks();
                availableSpace = availaBlock * blocSize / (1024 * 1024);
            }
            if(availableSpace < minSpaceMB ){
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }
}
