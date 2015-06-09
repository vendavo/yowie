package com.vendavo.mesos.yowie.api.domain

import com.fasterxml.jackson.annotation.JsonGetter
import groovy.transform.CompileStatic

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Created by vtajzich
 */
@CompileStatic
class TaskEvent {
    
    LocalDateTime timestamp
    TaskStatus status

    TaskEvent(TaskStatus status) {
        this.timestamp = LocalDateTime.now(ZoneId.of('UTC'))
        this.status = status
    }

    TaskEvent(LocalDateTime timestamp, TaskStatus status) {
        this.timestamp = timestamp
        this.status = status
    }

    @JsonGetter('status')
    String getStatusAsString() {
        return status.value
    }

    @JsonGetter('timestamp')
    String getTimestamp() {
        return DateTimeFormatter.ISO_DATE_TIME.format(timestamp)
    }

    @Override
    public String toString() {
        return "$timestamp - $status.value";
    }
}
