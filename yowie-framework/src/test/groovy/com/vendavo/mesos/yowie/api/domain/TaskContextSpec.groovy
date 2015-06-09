package com.vendavo.mesos.yowie.api.domain

import spock.lang.Specification

import java.time.LocalDateTime

/**
 * Created by vtajzich
 */
class TaskContextSpec extends Specification {

    TaskContext context

    def setup() {

        context = new TaskContext(new Task())
    }

    def "should be started when start time is not null"() {

        given:

        context.startTime = startTime

        when:

        boolean isStarted = context.started

        then:

        isStarted == expected

        where:

        startTime           | expected
        null                | false
        LocalDateTime.now() | true
    }

    def "should be running when start time is not null and end time is null"() {

        given:

        context.startTime = startTime
        context.endTime = endTime

        when:

        boolean isRunning = context.running

        then:

        isRunning == expected

        where:

        startTime           | endTime             | expected
        null                | null                | false
        LocalDateTime.now() | LocalDateTime.now() | false
        LocalDateTime.now() | null                | true
    }

    def "should be done when end time is not null"() {

        given:

        context.endTime = endTime

        when:

        boolean isDone = context.done

        then:

        isDone == expected

        where:

        endTime             | expected
        null                | false
        LocalDateTime.now() | true
    }

    def "should be done in final statuses"() {

        when:

        context.addEvent(new TaskEvent(LocalDateTime.now(), status))
        boolean isDone = context.done

        then:

        isDone == expected

        where:

        status                    | expected
        StaticTaskStatus.QUEUED   | false
        StaticTaskStatus.STAGING  | false
        StaticTaskStatus.LOST     | true
        StaticTaskStatus.FINISHED | true
        StaticTaskStatus.ERROR    | true
        StaticTaskStatus.KILLED   | true
    }

    def "should calculate duration between start and end date"() {

        given:

        context.startTime = startTime
        context.endTime = endTime

        when:

        long duration = context.duration

        then:

        duration == expected

        where:

        startTime                              | endTime                                | expected
        null                                   | null                                   | 0
        LocalDateTime.now()                    | null                                   | 0
        null                                   | LocalDateTime.now()                    | 0
        LocalDateTime.of(2015, 6, 3, 10, 0, 0) | LocalDateTime.of(2015, 6, 3, 11, 0, 0) | 3_600_000
    }
}
