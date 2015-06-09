package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic

/**
 * Created by vtajzich
 */
@CompileStatic
enum StaticTaskStatus implements TaskStatus {

    QUEUED(false, false), 
    STAGING(false, false), 
    STARTED(false, false), 
    RUNNING(false, false), 
    FINISHED(true, false), 
    LOST(true, true), 
    KILLED(true, true), 
    ERROR(true, true)

    private boolean isFinal
    boolean termination
    
    StaticTaskStatus(boolean isFinal, boolean termination) {
        this.termination = termination
        this.isFinal = isFinal
    }

    @Override
    String getValue() {
        return name()
    }

    @Override
    boolean isFinal() {
        return isFinal
    }
}
