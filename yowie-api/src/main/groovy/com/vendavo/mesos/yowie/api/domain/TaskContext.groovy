package com.vendavo.mesos.yowie.api.domain

import com.fasterxml.jackson.annotation.JsonGetter
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Created by vtajzich
 */
@EqualsAndHashCode(includes = ['task'])
@CompileStatic
class TaskContext {

    final Task task
    ResourceOffer resource

    LocalDateTime startTime
    LocalDateTime endTime

    List<TaskEvent> events = []

    TaskContext(Task task) {
        this.task = task
        addEvent(new TaskEvent(LocalDateTime.now(), StaticTaskStatus.QUEUED))
    }

    void addEvent(TaskEvent taskEvent) {
        events.add(taskEvent)

        switch (taskEvent.status) {

            case StaticTaskStatus.KILLED:
            case StaticTaskStatus.LOST:
            case StaticTaskStatus.FINISHED:
            case StaticTaskStatus.ERROR:
                endTime = LocalDateTime.now(ZoneId.of('UTC'))
                break
        }
    }

    boolean isStarted() {
        return startTime != null
    }

    boolean isRunning() {
        return startTime != null && endTime == null
    }

    boolean isDone() {
        return endTime != null
    }

    boolean isError() {
        return events.stream().anyMatch({ it.status == StaticTaskStatus.ERROR })
    }

    TaskStatus getLastStatus() {
        return events.last().status
    }

    @JsonGetter('startTime')
    String getStartTimeJson() {
        return startTime ? DateTimeFormatter.ISO_DATE_TIME.format(startTime) : ''
    }

    @JsonGetter('endTime')
    String getEndTimeJson() {
        return endTime ? DateTimeFormatter.ISO_DATE_TIME.format(endTime) : ''
    }

    /**
     * @return duration between startTime and endTime. If either is null then duration is zero.
     */
    long getDuration() {

        if (!startTime || !endTime) {
            return 0
        }

        return startTime.until(endTime, ChronoUnit.MILLIS)
    }

    @Override
    public String toString() {
        return events
    }
}
