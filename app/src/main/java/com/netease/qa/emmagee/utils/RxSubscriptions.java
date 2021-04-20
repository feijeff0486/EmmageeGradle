package com.netease.qa.emmagee.utils;

import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * 管理 CompositeSubscription
 * <p>
 *
 * @author Jeff
 * @date 2020/06/10 10:36
 *
 * <a href="mailto:feijeff0486@gmail.com">Contact me</a>
 * <a href="https://github.com/feijeff0486">Follow me</a>
 */
public final class RxSubscriptions {
    public static final String DEFAULT_TAG = "default_composite";
    private static ConcurrentHashMap<String, CompositeDisposable> mSubscriptionMap = new ConcurrentHashMap<>();

    private RxSubscriptions() {
        throw new CannotCreateException(this.getClass());
    }

    public static boolean isDisposed() {
        return isDisposed(DEFAULT_TAG);
    }

    public static boolean isDisposed(String tag) {
        return getCompositeDisposable(tag).isDisposed();
    }

    public static void add(Disposable s) {
        if (s != null) {
            add(DEFAULT_TAG, s);
        }
    }

    public static void add(String tag, Disposable s) {
        if (s != null && !StringUtils.isEmpty(tag)) {
            getCompositeDisposable(tag).add(s);
        }
    }

    public static void remove(Disposable s) {
        if (s != null) {
            remove(DEFAULT_TAG, s);
        }
    }

    public static void remove(String tag, Disposable s) {
        if (s != null) {
            getCompositeDisposable(tag).remove(s);
        }
    }

    public static void clear() {
        clear(DEFAULT_TAG);
    }

    public static void clear(String tag) {
        getCompositeDisposable(tag).clear();
    }

    public static void dispose() {
        dispose(DEFAULT_TAG);
    }

    public static void dispose(String tag) {
        getCompositeDisposable(tag).dispose();
    }

    public static synchronized CompositeDisposable getCompositeDisposable(String tag) {
        if (!mSubscriptionMap.containsKey(tag)) {
            mSubscriptionMap.put(tag, new CompositeDisposable());
        }
        return mSubscriptionMap.get(tag);
    }

}
