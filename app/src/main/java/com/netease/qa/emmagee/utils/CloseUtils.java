package com.netease.qa.emmagee.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * <p>
 *
 * @author Jeff
 * @date 2020/07/29 20:32
 *
 * <a href="mailto:feijeff0486@gmail.com">Contact me</a>
 * <a href="https://github.com/feijeff0486">Follow me</a>
 */
public final class CloseUtils {
    private CloseUtils() {
        throw new CannotCreateException(getClass());
    }

    /**
     * Close the io stream.
     *
     * @param closeables The closeables.
     */
    public static void closeIO(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Close the io stream quietly.
     *
     * @param closeables The closeables.
     */
    public static void closeIOQuietly(final Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}