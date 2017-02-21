package com.vendavo.mesos.yowie.api.domain;

/**
 * Created by vtajzich
 */
public enum StaticTaskStatus implements TaskStatus {

    QUEUED(false, false), 
    STAGING(false, false), 
    STARTED(false, false), 
    RUNNING(false, false), 
    FINISHED(true, false), 
    LOST(true, true), 
    KILLED(true, true), 
    ERROR(true, true);

    private boolean isFinal;
    private boolean termination;
    
    StaticTaskStatus(boolean isFinal, boolean termination) {
        this.termination = termination;
        this.isFinal = isFinal;
    }

    @Override
    public String getValue() {
        return name();
    }

    @Override
    public boolean isFinal() {
        return isFinal;
    }

    @Override
    public boolean isTermination() {
        return termination;
    }
}
