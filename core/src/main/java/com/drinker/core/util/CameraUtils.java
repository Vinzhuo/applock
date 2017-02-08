package com.drinker.core.util;

import android.hardware.Camera;

/**
 * Created by zhuolin on 16/6/2.
 */
public class CameraUtils {

    private static int sFrontCameraId = -1;

    public static int getFrontCameraId() {
        if (sFrontCameraId == -1) {
            Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
            int i = 0;
            int cameraCount = Camera.getNumberOfCameras();
            while (i < cameraCount) {
                Camera.getCameraInfo(i, localCameraInfo);
                if (localCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    sFrontCameraId = i;
                    break;
                }
                i++;
            }
        }
        return sFrontCameraId;
    }

    public static boolean hasFrontCamera() {
        return getFrontCameraId() != -1;
    }
}
