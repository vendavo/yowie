package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.*
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.exception.NoSuchTaskException
import org.apache.mesos.SchedulerDriver
import org.springframework.messaging.MessageChannel
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Created by vtajzich
 */
class DefaultYowieFrameworkSpec extends Specification {

    MessageChannel groupChannel
    MessageChannel taskChannel

    SchedulerDriver driver
    DefaultYowieFramework framework

    def setup() {

        groupChannel = Mock(MessageChannel)
        taskChannel = Mock(MessageChannel)

        driver = Mock(SchedulerDriver)

        framework = new DefaultYowieFramework(groupContextChannel: groupChannel, taskContextChannel: taskChannel)
        framework.zoneId = ZoneId.of('UTC')

        framework.initialize(driver, 'framework-id')
    }

    def "should update task status and find next one"() {

        given:

        framework.updateResources(new ResourcesAvailable([new ResourceOffer(null, 10d, 1024d, 0d, [], [:])]))

        Task task1 = new Task(name: 'task1')
        Task task2 = new Task(name: 'task2', dependsOn: new Dependency('task1', StaticTaskStatus.FINISHED))

        Group group = new Group()
        group.addTask(task1)
        group.addTask(task2)

        framework.createTask(group)

        TaskContext taskContext1 = framework.getTaskContext(task1.id)

        when:

        TaskDescription firstTask = framework.getNextTask()

        framework.taskHasReachedStatus(task1.id, StaticTaskStatus.FINISHED)

        TaskDescription secondTask = framework.getNextTask()

        TaskContext taskContext2 = framework.getTaskContext(task2.id)

        then:

        firstTask.task == task1
        secondTask.task == task2

        taskContext1.events.last().status == StaticTaskStatus.FINISHED
        taskContext2.events.size() == 1
        taskContext2.events.first().status == StaticTaskStatus.QUEUED
    }
    
    def "should create a task from group1 and not run any task from any other group until group 1 is finished"() {

        given:

        framework.updateResources(new ResourcesAvailable([new ResourceOffer(null, 10d, 1024d, 0d, [], [:])]))

        Task task1_1 = new Task(name: 'g1-task1')
        Task task1_2 = new Task(name: 'g1-task2', dependsOn: new Dependency('g1-task1', StaticTaskStatus.FINISHED))

        Group group1 = new Group()
        group1.addTask(task1_1)
        group1.addTask(task1_2)

        Task task2_1 = new Task(name: 'g2-task1')
        Task task2_2 = new Task(name: 'g2-task2', dependsOn: new Dependency('g2-task1', StaticTaskStatus.FINISHED))

        Group group2 = new Group()
        group2.addTask(task2_1)
        group2.addTask(task2_2)

        framework.createTask(group1)
        framework.createTask(group2)
        
        TaskContext taskContext1_1 = framework.getTaskContext(task1_1.id)
        TaskContext taskContext1_2 = framework.getTaskContext(task1_2.id)

        when:

        TaskDescription group_1_task_1 = framework.getNextTask()

        framework.taskHasReachedStatus(task1_1.id, StaticTaskStatus.STARTED)
        boolean hasNextTaskAfterTask_1_1_started = framework.hasTask()
        
        framework.taskHasReachedStatus(task1_1.id, StaticTaskStatus.FINISHED)
        
        TaskDescription group_1_task_2 = framework.getNextTask()
        boolean hasNextTaskAfterTask_1_2_started = framework.hasTask()
        
        framework.taskHasReachedStatus(task1_2.id, StaticTaskStatus.FINISHED)
        
        boolean hasNextTaskAfterTask_1_2_finished = framework.hasTask()
        
        TaskDescription group_2_task_1 = framework.getNextTask()

        then:
        
        !hasNextTaskAfterTask_1_1_started
        hasNextTaskAfterTask_1_2_started
        hasNextTaskAfterTask_1_2_finished

        group_1_task_1.task == task1_1
        group_1_task_2.task == task1_2
        
        group_2_task_1.task == task2_1
    }

    def "should return only finished tasks"() {

        given:

        Task task1 = new Task()
        Task task2 = new Task()

        framework.createTask(task1)
        framework.createTask(task2)

        TaskContext taskContext1 = framework.getTaskContext(task1.id)
        TaskContext taskContext2 = framework.getTaskContext(task2.id)

        taskContext1.addEvent(new TaskEvent(LocalDateTime.now(), StaticTaskStatus.FINISHED))
        taskContext2.addEvent(new TaskEvent(LocalDateTime.now(), StaticTaskStatus.RUNNING))

        when:

        def tasks = framework.finishedTaskContexts

        then:

        tasks.find { !it.done } == null
        tasks.size() == 1
        tasks[0] == taskContext1
    }

    def "should return only finished groups"() {

        given:

        Task task1 = new Task()
        Task task2 = new Task()

        Group group1 = new Group()
        group1.addTask(task1)

        Group group2 = new Group()
        group2.addTask(task2)

        GroupContext groupContext1 = framework.createTask(group1)
        GroupContext groupContext2 = framework.createTask(group2)

        TaskContext taskContext1 = groupContext1.getTaskContext(task1.id)
        TaskContext taskContext2 = groupContext2.getTaskContext(task2.id)

        taskContext1.addEvent(new TaskEvent(LocalDateTime.now(), StaticTaskStatus.FINISHED))
        taskContext2.addEvent(new TaskEvent(LocalDateTime.now(), StaticTaskStatus.RUNNING))

        when:

        def groups = framework.finishedGroupContexts

        then:

        groups.size() == 1
        groups[0] == groupContext1
    }

    def "should return task from group context even it's not current task"() {

        given:

        Task task1 = new Task()
        Task task2 = new Task()

        Group group = new Group()
        group.addTask(task1)
        group.addTask(task2)

        framework.createTask(group)

        when:

        TaskContext taskContext1 = framework.getTaskContext(task1.id)
        TaskContext taskContext2 = framework.getTaskContext(task2.id)

        then:

        taskContext1
        taskContext2

        taskContext1.task == task1
        taskContext2.task == task2
    }

    def "should throw an exception when no such task is present"() {

        given:

        Task task1 = new Task()
        Task task2 = new Task()
        Task notAddedTask = new Task()

        Group group = new Group()
        group.addTask(task1)
        group.addTask(task2)

        framework.createTask(group)

        when:

        TaskContext taskContext1 = framework.getTaskContext(task1.id)
        TaskContext taskContext2 = framework.getTaskContext(task2.id)

        framework.getTaskContext(notAddedTask.id)

        then:

        taskContext1
        taskContext2

        taskContext1.task == task1
        taskContext2.task == task2

        thrown(NoSuchTaskException)
    }


    def "should remove task from queue when kill is called"() {

        given:

        Task task = new Task()
        framework.createTask(task)

        when:

        TaskContext context = framework.getTaskContext(task.id)
        framework.hasTask()

        int sizeBeforeKill = framework.queueSize()
        framework.kill(task.id)

        int sizeAfterKill = framework.queueSize()

        then:

        sizeBeforeKill == 1
        sizeAfterKill == 0

        context.events.size() == 2
        context.events[0].status == StaticTaskStatus.QUEUED
        context.events[1].status == StaticTaskStatus.KILLED

        1 * groupChannel.send(!null)
        1 * taskChannel.send(!null)
    }

    def "should ask driver to kill task when it's being processing"() {

        given:

        Task task = new Task()
        framework.createTask(task)

        TaskContext context = framework.getTaskContext(task.id)
        context.addEvent(new TaskEvent(StaticTaskStatus.RUNNING))

        when:

        framework.kill(task.id)

        then:

        context.events.size() == 2
        context.events[0].status == StaticTaskStatus.QUEUED
        context.events[1].status == StaticTaskStatus.RUNNING

        1 * driver.killTask(!null)
    }

    def "should kill all tasks in group when one of them reach error state / got killed"() {

        given:

        Task task1 = new Task()
        Task task2 = new Task()
        Task task3 = new Task()

        Group group = new Group()
        group.addTask(task1)
        group.addTask(task2)
        group.addTask(task3)

        framework.createTask(group)

        TaskContext taskContext1 = framework.getTaskContext(task1.id)
        TaskContext taskContext2 = framework.getTaskContext(task2.id)
        TaskContext taskContext3 = framework.getTaskContext(task3.id)

        when:

        framework.taskHasReachedStatus(task1.id, StaticTaskStatus.RUNNING)
        framework.taskHasReachedStatus(task2.id, reachedStatus)

        then:

        taskContext1.task == task1
        taskContext2.task == task2
        taskContext3.task == task3

        taskContext2.events.last().status == reachedStatus
        taskContext3.events.last().status == lastExpectedStatus

        noOfKills * driver.killTask(!null)

        (1.._) * taskChannel.send(!null)
        (1.._) * groupChannel.send(!null)

        where:

        reachedStatus             | noOfKills | lastExpectedStatus
        StaticTaskStatus.KILLED   | 1         | StaticTaskStatus.KILLED
        StaticTaskStatus.LOST     | 1         | StaticTaskStatus.KILLED
        StaticTaskStatus.ERROR    | 1         | StaticTaskStatus.KILLED
        StaticTaskStatus.FINISHED | 0         | StaticTaskStatus.QUEUED
    }

    def "should follow terminate task and kill all alive"() {

        given:

        Task task1 = new Task(name: 'task1')
        Task task2 = new Task(name: 'task2')
        Task task3 = new Task(name: 'task3')

        Group group = new Group(terminateTask: new Dependency('task2', terminateStatus))
        group.addTask(task1)
        group.addTask(task2)
        group.addTask(task3)

        framework.createTask(group)

        TaskContext taskContext1 = framework.getTaskContext(task1.id)
        TaskContext taskContext2 = framework.getTaskContext(task2.id)
        TaskContext taskContext3 = framework.getTaskContext(task3.id)

        when:

        framework.taskHasReachedStatus(task1.id, StaticTaskStatus.RUNNING)
        framework.taskHasReachedStatus(task3.id, StaticTaskStatus.RUNNING)

        framework.taskHasReachedStatus(task2.id, reachedStatus)

        then:

        taskContext1.task == task1
        taskContext2.task == task2
        taskContext3.task == task3

        taskContext2.events.last().status == reachedStatus

        noOfKills * driver.killTask(!null)

        (1.._) * taskChannel.send(!null)
        (1.._) * groupChannel.send(!null)

        where:

        terminateStatus                                | reachedStatus                                  | noOfKills
        StaticTaskStatus.FINISHED                      | StaticTaskStatus.RUNNING                       | 0
        StaticTaskStatus.FINISHED                      | StaticTaskStatus.STAGING                       | 0
        StaticTaskStatus.FINISHED                      | StaticTaskStatus.FINISHED                      | 2
        new CustomTaskStatus('my_custom_status')       | StaticTaskStatus.RUNNING                       | 0
        new CustomTaskStatus('my_custom_status')       | new CustomTaskStatus('my_custom_status')       | 3 //as status is not final, therefor task still running it'll be killed as well
        new CustomTaskStatus('my_custom_status', true) | new CustomTaskStatus('my_custom_status', true) | 2 //as status is final, therefor other tasks will be killed only
    }
}
