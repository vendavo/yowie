package com.vendavo.mesos.yowie.event;

import org.springframework.context.ApplicationEvent;

/**
 * Created by vtajzich
 */
public class ExecutorLostEvent extends ApplicationEvent {

    public ExecutorLostEvent(Object source) {
        super(source);
    }
}
