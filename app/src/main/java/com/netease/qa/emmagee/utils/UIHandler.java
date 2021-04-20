package com.netease.qa.emmagee.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

/**
 * <p>
 *
 * @author Jeff
 * @date 2020/04/15 19:48
 *
 * <a href="mailto:feijeff0486@gmail.com">Contact me</a>
 * <a href="https://github.com/feijeff0486">Follow me</a>
 */
public final class UIHandler extends Handler {
    private static volatile UIHandler sInstance;

    private UIHandler(Looper looper) {
        super(looper);
    }

    public static UIHandler getInstance() {
        if (sInstance == null) {
            synchronized (UIHandler.class) {
                if (sInstance == null) {
                    sInstance = new UIHandler(Looper.getMainLooper());
                }
            }
        }
        return sInstance;
    }

    public static synchronized void runOnUiThread(@NonNull Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            getInstance().post(runnable);
        }
    }

    public static synchronized void runOnUiThreadDelay(@NonNull Runnable runnable, long delay) {
        getInstance().postDelayed(runnable, delay);
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}