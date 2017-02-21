package com.vendavo.mesos.yowie.event;

import org.springframework.context.ApplicationEvent;

/**
 * Created by vtajzich
 */
public class DisconnectedEvent extends ApplicationEvent {

    public DisconnectedEvent(Object source) {
        super(source);
    }
}
