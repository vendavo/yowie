package com.vendavo.mesos.yowie.api.domain

import spock.lang.Specification

import java.time.LocalDateTime
import java.util.stream.Stream

/**
 * Created by vtajzich
 */
class GroupContextSpec extends Specification {

    Task task1
    Task task2
    Task task3
    Task task4

    Group group
    GroupContext context

    def setup() {

        task1 = new Task(name: 'task1')
        task2 = new Task(name: 'task2', dependsOn: new Dependency('task1', new CustomTaskStatus('UP')))
        task3 = new Task(name: 'task3', dependsOn: new Dependency('task2', new CustomTaskStatus('STATE_1')))
        task4 = new Task(name: 'task4', dependsOn: new Dependency('task3', new CustomTaskStatus('STATE_2')))

        group = new Group()
        group.addTask(task1)
        group.addTask(task2)
        group.addTask(task3)
        group.addTask(task4)

        context = new GroupContext(group)
    }

    def "should have next task only for correct state"() {

        when:

        Stream<TaskContext> stream = context.getNext(task1.id, status)

        then:

        stream.findFirst().isPresent() == present

        where:

        status                                   | present
        new CustomTaskStatus('SOME_STATE')       | false
        new CustomTaskStatus('SOME_OTHER_STATE') | false
        new CustomTaskStatus('UP')               | true

    }

    def "group should not be done when none task is done"() {

        given:

        context.taskContexts.each {
            it.startTime = null
            it.endTime = null
        }


        when:

        boolean done = context.isDone()

        then:

        !done
    }

    def "group should not be done when one task is not done"() {

        given:

        context.taskContexts.each {
            it.startTime = null
            it.endTime = null
        }

        def lastTaskContext = context.taskContexts.last()
        lastTaskContext.startTime = LocalDateTime.now()
        lastTaskContext.endTime = LocalDateTime.now()

        when:

        boolean done = context.isDone()

        then:

        !done
    }

    def "group should be done when all tasks are done"() {

        given:

        context.taskContexts.each {
            it.startTime = LocalDateTime.now()
            it.endTime = LocalDateTime.now()
        }

        when:

        boolean done = context.isDone()

        then:

        done
    }

    def "group should not be running when none of tasks are running"() {

        given:

        context.taskContexts.each {
            it.startTime = null
            it.endTime = null
        }


        when:

        boolean running = context.isRunning()

        then:

        !running
    }

    def "group should be running when 3 tasks are running and one task is not running"() {

        given:

        context.taskContexts.each {
            it.startTime = LocalDateTime.now()
            it.endTime = null
        }

        def lastTaskContext = context.taskContexts.last()
        lastTaskContext.startTime = null

        when:

        boolean running = context.isRunning()

        then:

        running
    }

    def "group should be running when all tasks are running"() {

        given:

        context.taskContexts.each {
            it.startTime = LocalDateTime.now()
            it.endTime = null
        }

        when:

        boolean running = context.isRunning()

        then:

        running
    }

    def "should not have more tasks to process when group consists of only one task which is running"() {

        given:

        Task task = new Task()

        Group group = new Group().addTask(task)
        GroupContext context = new GroupContext(group)

        context.getTaskContext(task.id).startTime = LocalDateTime.now()

        when:

        boolean hasMoreTasksToProcess = context.hasMoreTasksToProcess()

        then:

        context.isRunning()
        !hasMoreTasksToProcess
    }

    def "should have more tasks to process when only first task is running"() {

        given:

        context.taskContexts[0].startTime = LocalDateTime.now()

        when:

        boolean hasMoreTasksToProcess = context.hasMoreTasksToProcess()

        then:

        context.isRunning()
        hasMoreTasksToProcess
    }

    def "should return correct duration"() {

        given:

        context.taskContexts.first().startTime = startTime
        context.taskContexts.last().endTime = endTime

        when:

        long duration = context.duration

        then:

        duration == expectedDuration

        where:

        startTime                              | endTime                                | expectedDuration
        null                                   | null                                   | 0
        LocalDateTime.of(2015, 6, 3, 10, 0, 0) | LocalDateTime.of(2015, 6, 3, 11, 0, 0) | 3_600_000
        LocalDateTime.now()                    | null                                   | 0
    }
}
