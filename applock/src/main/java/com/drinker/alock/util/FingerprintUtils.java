package com.drinker.alock.util;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

/**
 * Created by Michael on 15/10/16.
 */
public class FingerprintUtils {
    private static final String TAG = "FingerprintUtils";
    private static Spass mSpass = new Spass();
    private static SpassFingerprint mSpassFingerprint;
    private static boolean isSamsungFingerPrintHardware = false;

    public static  boolean isFingerprintHardwareAvailable(Context ctx) {
        boolean result = false;
        if (isSDKAPIAbove23(ctx)) {
            try {
                FingerprintManagerCompat mFingerprintManager = FingerprintManagerCompat.from(ctx);
                result = mFingerprintManager.isHardwareDetected();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (result) {
                isSamsungFingerPrintHardware = false;
                return result;
            } else {
                try {
                    mSpass.initialize(ctx.getApplicationContext());
                    result  = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
                    if (result) {
                        isSamsungFingerPrintHardware = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    result = false;
                }
            }
        } else {
            try {
                mSpass.initialize(ctx.getApplicationContext());
                result  = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
                if (result) {
                    isSamsungFingerPrintHardware = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = false;
            }
        }
        return result;
    }

    public static boolean isSamsungHardware() {
        return isSamsungFingerPrintHardware;
    }

    public static boolean isSDKAPIAbove23 (Context ctx) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static boolean screenLocked (Context ctx) {
        boolean result = false;
        try {
            KeyguardManager mKeyguardManager = (KeyguardManager)ctx.getSystemService(Context.KEYGUARD_SERVICE);
            result = mKeyguardManager.isDeviceSecure();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean alreadyHasFingerprint (Context ctx) {
        if (!isFingerprintHardwareAvailable(ctx) ) return false;
        boolean result = false;
        try {
            FingerprintManagerCompat mFingerprintManager = FingerprintManagerCompat.from(ctx);
            result = mFingerprintManager.hasEnrolledFingerprints();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result) {
            return true;
        }
        try {
            if (mSpassFingerprint != null) {
                result = mSpassFingerprint.hasRegisteredFinger();
            } else {
                mSpassFingerprint = new SpassFingerprint(ctx.getApplicationContext());
                result = mSpassFingerprint.hasRegisteredFinger();
            }
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static SpassFingerprint getSpassFingerprint(Context ctx) {
        if (!isSamsungFingerPrintHardware) {
            return null;
        }
        if (mSpassFingerprint != null) {
            return mSpassFingerprint;
        } else {
            try {
                mSpassFingerprint = new SpassFingerprint(ctx.getApplicationContext());
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
            }
        }
        return mSpassFingerprint;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

}

