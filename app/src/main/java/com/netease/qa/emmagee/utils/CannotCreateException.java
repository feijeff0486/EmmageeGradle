package com.netease.qa.emmagee.utils;

/**
 * <p>
 *
 * @author Jeff
 * @date 2020/07/29 19:19
 *
 * <a href="mailto:feijeff0486@gmail.com">Contact me</a>
 * <a href="https://github.com/feijeff0486">Follow me</a>
 */
public final class CannotCreateException extends UnsupportedOperationException {

    public CannotCreateException(Class clz) {
        this("You can't instantiate " + clz.getName() + " !");
    }

    public CannotCreateException(String message) {
        super(message);
    }
}
