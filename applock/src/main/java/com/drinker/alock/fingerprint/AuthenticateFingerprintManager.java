package com.drinker.alock.fingerprint;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;

import com.drinker.alock.util.FingerprintUtils;
import com.drinker.alock.util.log.Log;
import com.samsung.android.sdk.pass.SpassFingerprint;


public class AuthenticateFingerprintManager extends FingerprintManagerCompat.AuthenticationCallback {

    static final long ERROR_TIMEOUT_MILLIS = 1600;
    static final long SUCCESS_DELAY_MILLIS = 1300;
    private static final String TAG = "AuthenticateFingerprintManager";

    private final FingerprintManagerCompat mFingerprintManager;
    private AuthenticateCallback mAuthenticateCallback;
    private CancellationSignal mCancellationSignal;
    private static volatile AuthenticateFingerprintManager mAuthenticateFingerprintManager;
    private Context context;
    private boolean needDelay = false;
    private boolean mSelfCancelled;
    private Handler mHandler;
//    private boolean onReadyIdentify = false;
    private boolean needRetryIdentify = true;


    public static AuthenticateFingerprintManager getInstance (Context ctx) {
        if (mAuthenticateFingerprintManager != null) {
            return mAuthenticateFingerprintManager;
        } else {
            synchronized (AuthenticateFingerprintManager.class) {
                if (mAuthenticateFingerprintManager == null) {
                    mAuthenticateFingerprintManager = new AuthenticateFingerprintManager(ctx);
                }
                return mAuthenticateFingerprintManager;
            }
        }
    }
    private SpassFingerprint.IdentifyListener mIdentifyListener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
//            onReadyIdentify = false;
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                Log.d(TAG,"onFinished() : Identify authentification Success");
                needRetryIdentify =false;
                if (needDelay) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mAuthenticateCallback != null) {
                                mAuthenticateCallback.onAuthenticated();
                            }
                        }
                    }, SUCCESS_DELAY_MILLIS);
                } else {
                    if (mAuthenticateCallback != null) {
                        mAuthenticateCallback.onAuthenticated();
                    }                }
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                Log.d(TAG,"onFinished() : Password authentification Success");

            } else if (eventStatus == SpassFingerprint.STATUS_OPERATION_DENIED) {
                Log.d(TAG,"onFinished() : Authentification is blocked because of fingerprint service internally.");
                showError("");
            } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED) {
                Log.d(TAG,"onFinished() : User cancel this identify.");
            } else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
                mAuthenticateCallback.onReset();
                Log.d(TAG,"onFinished() : The time for identify is finished.");
            } else if (eventStatus == SpassFingerprint.STATUS_QUALITY_FAILED) {
                Log.d(TAG,"onFinished() : Authentification Fail for identify.");
                needRetryIdentify = true;
                showError("");
            } else {
                Log.d(TAG,"onFinished() : Authentification Fail for identify");
                needRetryIdentify = true;
                showError("");
            }
        }

        @Override
        public void onReady() {
            Log.d(TAG,"identify state is ready");
        }

        @Override
        public void onStarted() {
            Log.d(TAG,"User touched fingerprint sensor");
        }

        @Override
        public void onCompleted() {
            Log.d(TAG,"the identify is completed");
//            onReadyIdentify = false;
            if (needRetryIdentify) {
//                needRetryIdentify = false;
                mHandler.removeCallbacks(restartFinger);
                mHandler.postDelayed(restartFinger, 100);
            } else {
                Log.d(TAG,"we dont need retry");
            }
        }
    };
    private Runnable restartFinger = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG,"restart");
            if (context != null && FingerprintUtils.getSpassFingerprint(context) != null ) {
                try {
                    FingerprintUtils.getSpassFingerprint(context).startIdentify(mIdentifyListener);
                    Log.d(TAG,"restart completed");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG,"restart fail");
                }
            }
        }
    };

    private AuthenticateFingerprintManager(Context ctx) {

        mFingerprintManager = FingerprintManagerCompat.from(ctx);
        mHandler = new Handler(Looper.getMainLooper());
        context = ctx;
    }

    public void startListening(AuthenticateCallback authenticateCallback) {
        Log.d(TAG, "startListening");
        if (!FingerprintUtils.alreadyHasFingerprint(context)) {
            return;
        }
        mSelfCancelled = false;
        needRetryIdentify = true;
        mAuthenticateCallback = authenticateCallback;
        if (FingerprintUtils.isSamsungHardware()) {
            if (FingerprintUtils.getSpassFingerprint(context) != null ) {
                try {
                    FingerprintUtils.getSpassFingerprint(context).startIdentify(mIdentifyListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            mCancellationSignal = new CancellationSignal();
            mFingerprintManager.authenticate(null, 0, mCancellationSignal, this, null);
        }
    }

    public void stopListening() {
        mSelfCancelled = true;
        if (FingerprintUtils.isSamsungHardware()) {
            try {
                if (FingerprintUtils.getSpassFingerprint(context) != null) {
                    FingerprintUtils.getSpassFingerprint(context).cancelIdentify();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            needRetryIdentify = false;
        } else {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
                mCancellationSignal = null;
            }
        }
        mAuthenticateCallback = null;
        Log.d(TAG, "stopListening");
    }

    public void setNeedDelayAfterSuccess(boolean need) {
        needDelay = need;
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Log.d(TAG, "Error ErrorStr=" + errString.toString() + " msgId=" + errMsgId);
        if (!mSelfCancelled) {
            mAuthenticateCallback.errorTooManyTimes(errMsgId, errString.toString());
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Log.d(TAG,"Help HelpStr=" + helpString.toString() + " msgId=" + helpString);
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError("");
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        Log.d(TAG,"sucessful");
        if (needDelay) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAuthenticateCallback.onAuthenticated();
                }
            }, SUCCESS_DELAY_MILLIS);
        } else {
            mAuthenticateCallback.onAuthenticated();
        }

    }

    private void showError(CharSequence error) {
        mAuthenticateCallback.onError(error.toString());
    }

    public interface AuthenticateCallback {

        void onAuthenticated();

        void onError(String error);

        void onReset();

        void errorTooManyTimes(int errorcode, String error);
    }
}
