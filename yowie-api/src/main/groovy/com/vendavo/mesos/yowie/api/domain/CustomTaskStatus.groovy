package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic

/**
 * Created by vtajzich
 */
@CompileStatic
class CustomTaskStatus implements TaskStatus {

    private String status
    private final boolean isFinal
    final boolean termination

    CustomTaskStatus(String status) {
        this.status = status
    }

    CustomTaskStatus(String status, boolean isFinal) {
        this.status = status
        this.isFinal = isFinal
    }

    CustomTaskStatus(String status, boolean isFinal, boolean termination) {
        this.status = status
        this.isFinal = isFinal
        this.termination = termination
    }

    boolean isFinal() {
        return isFinal
    }

    @Override
    String getValue() {
        return status
    }

    @Override
    public String toString() {
        return status
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        CustomTaskStatus that = (CustomTaskStatus) o

        if (status != that.status) return false

        return true
    }

    int hashCode() {
        return status.hashCode()
    }
}
