package com.vendavo.mesos.yowie.event;

import org.springframework.context.ApplicationEvent;

/**
 * Created by vtajzich
 */
public class SlaveLostEvent extends ApplicationEvent {

    public SlaveLostEvent(Object source) {
        super(source);
    }
}
