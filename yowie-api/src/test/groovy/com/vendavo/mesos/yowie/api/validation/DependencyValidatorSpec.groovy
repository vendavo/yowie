package com.vendavo.mesos.yowie.api.validation

import com.vendavo.mesos.yowie.api.domain.Dependency
import com.vendavo.mesos.yowie.api.domain.Task
import spock.lang.Specification

/**
 * Created by vtajzich
 */
class DependencyValidatorSpec extends Specification {

    List<Task> tasks
    DependencyValidator validator

    def setup() {

        tasks = []
        validator = new DependencyValidator()
    }

    def "should valid empty task list"() {

        when:

        boolean valid = validator.isValid(tasks, null)

        then:
        
        valid
    }

    def "should valid task list w/ correct dependencies"() {

        given:
        
        tasks << new Task(name: 'task1')
        tasks << new Task(name: 'task2', dependsOn: new Dependency(name: 'task1'))
        tasks << new Task(name: 'task3', dependsOn: new Dependency(name: 'task2'))
        tasks << new Task(name: 'task4', dependsOn: new Dependency(name: 'task1'))
        
        when:

        boolean valid = validator.isValid(tasks, null)

        then:

        valid
    }

    def "should valid task list w/o correct dependencies"() {

        given:

        tasks << new Task(name: 'task1')
        tasks << new Task(name: 'task2', dependsOn: new Dependency(name: 'task1'))
        tasks << new Task(name: 'task3', dependsOn: new Dependency(name: 'task7'))

        when:

        boolean valid = validator.isValid(tasks, null)

        then:

        !valid
    }
}
