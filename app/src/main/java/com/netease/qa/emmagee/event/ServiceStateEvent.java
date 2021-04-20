package com.netease.qa.emmagee.event;

import java.io.Serializable;

/**
 * @author Jeff
 * @date 2021/04/16
 *
 * <a href="mailto:feijeff0486@gmail.com">Contact me</a>
 * <a href="https://github.com/feijeff0486">Follow me</a>
 */
public class ServiceStateEvent implements Serializable {
    public boolean running = false;

    public ServiceStateEvent(boolean running) {
        this.running = running;
    }
}
