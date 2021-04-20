package com.netease.qa.emmagee.utils;

import java.util.Collection;

/**
 * @author Jeff
 * @describe
 * @date 2020/3/27.
 */
public final class StringUtils {

    public static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNull(final String s) {
        if (s == null) return true;
        return "null".equals(s.toLowerCase()) || isSpace(s);
    }

    public static boolean isEmpty(String str) {
        return null == str || "".equals(str) || "NULL".equals(str.toUpperCase());
    }

    public static boolean isEmptyArray(Collection list) {
        return list == null || list.size() == 0;
    }

    public static <T> boolean isEmptyArray(T[] list) {
        return list == null || list.length == 0;
    }
}
