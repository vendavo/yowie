package com.vendavo.mesos.yowie.api.validation

import com.vendavo.mesos.yowie.api.domain.Dependency
import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.StaticTaskStatus
import com.vendavo.mesos.yowie.api.domain.Task
import spock.lang.Specification

/**
 * Created by vtajzich
 */
class TerminateTaskValidatorSpec extends Specification {


    Group group

    Task task1
    Task task2
    Task task3


    TerminateTaskValidator validator

    def setup() {

        task1 = new Task(name: 'task1')
        task2 = new Task(name: 'task2')
        task3 = new Task(name: 'task3')

        group = new Group()
        group.addTask(task1).addTask(task2).addTask(task3)

        validator = new TerminateTaskValidator()
    }

    def "should validate terminate task"() {

        given:

        group.terminateTask = terminateTask

        when:


        boolean isValid = validator.isValid(group, null)

        then:

        isValid == expected

        where:

        terminateTask                                             | expected
        null                                                      | true
        new Dependency('task1', StaticTaskStatus.FINISHED)        | true
        new Dependency('NOT_EXISTING', StaticTaskStatus.FINISHED) | false

    }
}
