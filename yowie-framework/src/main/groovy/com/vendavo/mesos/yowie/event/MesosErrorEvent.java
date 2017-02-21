package com.vendavo.mesos.yowie.event;

import org.springframework.context.ApplicationEvent;

/**
 * Created by vtajzich
 */
public class MesosErrorEvent extends ApplicationEvent {

    public MesosErrorEvent(Object source) {
        super(source);
    }
}
