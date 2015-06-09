package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.Dependency
import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.StaticTaskStatus
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.exception.NoTaskAvailableException
import org.springframework.messaging.MessageChannel
import spock.lang.Specification

import java.time.ZoneId

/**
 * Created by vtajzich
 */
class TaskRunnerFrameworkSpec extends Specification {

    MessageChannel groupContextChannel
    MessageChannel taskContextChannel
    
    DefaultYowieFramework framework

    def setup() {
        
        groupContextChannel = Mock(MessageChannel)
        taskContextChannel = Mock(MessageChannel)
        
        framework = new DefaultYowieFramework(zoneId: ZoneId.systemDefault())
        framework.groupContextChannel = groupContextChannel
        framework.taskContextChannel = taskContextChannel
    }

    def "should not have a task as none is added"() {

        when:

        boolean hasTask = framework.hasTask()

        then:

        !hasTask
    }

    def "should have a task until resources are allocated"() {

        given:

        ResourcesAvailable available = new ResourcesAvailable([new ResourceOffer(null, 10, 2048, 200000, [], [:])])

        Task task1 = new Task(name: '1', cpus: 3, mem: 128)
        Task task2 = new Task(name: '2', cpus: 3, mem: 128)
        Task task3 = new Task(name: '3', cpus: 3, mem: 128)
        Task task4 = new Task(name: '4', cpus: 3, mem: 128)

        framework.updateResources(available)
        framework.createTask(task1)
        framework.createTask(task2)
        framework.createTask(task3)
        framework.createTask(task4)

        when:

        boolean hasTask1 = framework.hasTask()
        Task nextTask1 = framework.getNextTask().task
        
        boolean hasTask2 = framework.hasTask()
        Task nextTask2 = framework.getNextTask().task
        
        boolean hasTask3 = framework.hasTask()
        Task nextTask3 = framework.getNextTask().task
        
        boolean hasTask4 = framework.hasTask()

        then:

        hasTask1
        hasTask2
        hasTask3
        !hasTask4
        
        nextTask1 == task1
        nextTask2 == task2
        nextTask3 == task3
    }

    def "should throw exception as no task is available"() {

        when:

        framework.getNextTask()

        then:

        thrown(NoTaskAvailableException)
    }

    def "should throw exception as no resources are available"() {

        given:

        Task task = new Task(name: '1', cpus: 3, mem: 128)
        
        framework.createTask(task)
        
        when:

        framework.getNextTask()

        then:

        thrown(NoTaskAvailableException)
    }
    
    def "should process all tasks in group"() {

        given:

        def offer = new ResourceOffer(null, 10, 2048, 200000, [], [:])
        ResourcesAvailable available = new ResourcesAvailable([offer])
        framework.updateResources(available)

        Task task1 = new Task(name: '1', cpus: 3, mem: 128)
        Task task2 = new Task(name: '2', cpus: 3, mem: 128, dependsOn: new Dependency(name: '1', status: StaticTaskStatus.RUNNING))
        Task task3 = new Task(name: '3', cpus: 3, mem: 128, dependsOn: new Dependency(name: '2', status: StaticTaskStatus.RUNNING))
        Task task4 = new Task(name: '4', cpus: 3, mem: 128, dependsOn: new Dependency(name: '3', status: StaticTaskStatus.RUNNING))

        Group group = new Group()
        group.addTask(task1)
        group.addTask(task2)
        group.addTask(task3)
        group.addTask(task4)
        
        framework.createTask(group)

        when:

        boolean hasTask1 = framework.hasTask()
        Task nextTask1 = framework.getNextTask().task
        
        boolean beforeTask1Reached = framework.hasTask()
        
        //To simulate mesos' messages
        offer.cpus = 10
        offer.mem = 2048
        
        framework.taskHasReachedStatus(nextTask1.id, StaticTaskStatus.RUNNING)

        boolean hasTask2 = framework.hasTask()
        Task nextTask2 = framework.getNextTask().task

        boolean beforeTask2Reached = framework.hasTask()

        //To simulate mesos' messages
        offer.cpus = 10
        offer.mem = 2048
        
        framework.taskHasReachedStatus(nextTask2.id, StaticTaskStatus.RUNNING)

        boolean hasTask3 = framework.hasTask()
        Task nextTask3 = framework.getNextTask().task

        boolean beforeTask3Reached = framework.hasTask()

        //To simulate mesos' messages
        offer.cpus = 10
        offer.mem = 2048
        
        framework.taskHasReachedStatus(nextTask3.id, StaticTaskStatus.RUNNING)

        boolean hasTask4 = framework.hasTask()
        Task nextTask4 = framework.getNextTask().task

        boolean hasTaskNext = framework.hasTask()

        then:
        
        !beforeTask1Reached
        !beforeTask2Reached
        !beforeTask3Reached

        hasTask1
        hasTask2
        hasTask3
        hasTask4
        !hasTaskNext

        nextTask1 == task1
        nextTask2 == task2
        nextTask3 == task3
        nextTask4 == task4
    }
}
