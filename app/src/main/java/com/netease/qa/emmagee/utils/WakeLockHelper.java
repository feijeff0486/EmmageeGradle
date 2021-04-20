package com.netease.qa.emmagee.utils;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.netease.qa.emmagee.BuildConfig;

public class WakeLockHelper {
    private static final String TAG = Constants.LOG_PREFIX + "WakeLockHelper";
    private WakeLock mWakeLock;
    private Context mContext;

    public WakeLockHelper(@NonNull Context context) {
        this.mContext = context.getApplicationContext();
    }

    /**
     * 阻止屏保显示
     */
    public void acquireFullWakeLock() {
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        if (mWakeLock == null) {
            if ("damai".equals(BuildConfig.FLAVOR)) {
                mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ON_AFTER_RELEASE | PowerManager.ACQUIRE_CAUSES_WAKEUP, "DPA_HIVEVIEW");
            } else {
                mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
            }
        }
        if (mWakeLock != null) {
            Log.i(TAG, "mWakeLock acquire()------------------- ");
            mWakeLock.acquire();
        }
    }

    /**
     * 释放WakeLock
     */
    public void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            Log.i(TAG, "mWakeLock release()------------------- ");
            mWakeLock.release();
            mWakeLock = null;
        }
    }
}
