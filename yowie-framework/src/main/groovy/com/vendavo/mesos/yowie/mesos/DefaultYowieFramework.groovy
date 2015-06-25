package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.*
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.exception.NoSuchTaskException
import com.vendavo.mesos.yowie.exception.NoTaskAvailableException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.mesos.Protos
import org.apache.mesos.SchedulerDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel
import org.springframework.stereotype.Component

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.stream.Stream

/**
 * Created by vtajzich
 */
@Slf4j
@Component('framework')
@CompileStatic
class DefaultYowieFramework implements YowieFramework {

    @Qualifier('groupUpdates')
    @Autowired
    MessageChannel groupContextChannel

    @Qualifier('taskUpdates')
    @Autowired
    MessageChannel taskContextChannel

    @Autowired
    ZoneId zoneId

    private boolean registered
    private String id
    private ResourcesAvailable availableResources = new ResourcesAvailable([])
    private List<GroupContext> groupContexts = Collections.synchronizedList([])
    private Queue<Task> tasksToBeProcessed = new ConcurrentLinkedDeque<>()
    private SchedulerDriver driver

    void initialize(SchedulerDriver driver, String id) {
        this.driver = driver
        this.id = id
        registered = true
    }

    @Override
    String getId() {
        return id
    }

    @Override
    void kill(String id) {

        TaskContext taskContext = getTaskContext(id)

        if (taskContext.lastStatus == StaticTaskStatus.QUEUED) {

            killQueuedTask(taskContext)

        } else {

            log.info(""" taskId="$id" Going to kill task. """)
            driver.killTask(Protos.TaskID.newBuilder().setValue(taskContext.task.id).build())
        }
    }

    void updateResources(ResourcesAvailable resources) {
        this.availableResources = resources
    }

    GroupContext createTask(Group group) {

        GroupContext context = new GroupContext(group)
        def first = group.tasks.first()

        log.info(""" groupId="$group.id" taskId="$first.id" Creating group context. """)

        groupContexts.add(context)

        send(context)
        send(context.getTaskContext(first.id))

        return context
    }

    void createTask(Task task) {

        Group group = new Group(name: "Wrapper group for '$task.name ($task.id)'")
        group.addTask(task)

        createTask(group)
    }

    void taskHasReachedStatus(String id, TaskStatus status) {

        GroupContext context = getGroupContext(id)
        TaskContext currentTaskContext = context.getTaskContext(id)

        log.info(""" taskId="$id" taskName="$currentTaskContext.task.name" status="$status" Task has reached status. """)

        currentTaskContext.addEvent(new TaskEvent(LocalDateTime.now(zoneId), status))

        send(context)
        send(currentTaskContext)

        if (status.termination || context.shouldFinish(currentTaskContext, status)) {
            context.taskContexts.stream()
                    .filter({ !it.events.last().status.isFinal() })
                    .forEach({ kill(it.task.id) })
            return
        }

        Stream<TaskContext> stream = context.getNext(id, status)

        stream.forEach({ TaskContext taskContext ->

            log.info(""" taskId="$id" nextTaskId="$taskContext.task.id" taskName="$currentTaskContext.task.name" nextTaskName="$taskContext.task.name" status="$status" Adding next task in row.""")

            tasksToBeProcessed.add(taskContext.task)
            send(taskContext)
        })
    }

    private GroupContext getGroupContext(String taskId) {
        return groupContexts.find { it.group.tasks.find { it.id == taskId } != null }
    }

    TaskContext getTaskContext(String id) {

        Optional<TaskContext> taskContext = groupContexts.stream()
                .flatMap({ it.taskContexts.stream() })
                .filter({ it.task.id == id })
                .findFirst()

        if (!taskContext.present) {
            throw new NoSuchTaskException(""" taskId="$id" No task found for id! """)
        }

        return taskContext.get()
    }

    ResourcesAvailable getAvailableResources() {
        return availableResources
    }

    Stream<GroupContext> getAllGroupContexts() {
        return groupContexts.stream().sorted({ GroupContext lh, GroupContext rh -> lh.created.compareTo(rh.created) } as Comparator<GroupContext>)
    }

    TaskDescription getNextTask() {

        if (!hasTask()) {
            throw new NoTaskAvailableException(""" Queue size is: "${
                tasksToBeProcessed.size()
            }", Resources: $availableResources """)
        }

        Task next = tasksToBeProcessed.poll()
        ResourceOffer offer = availableResources.allocate(next)

        TaskContext context = getTaskContext(next.id)
        context.resource = offer
        context.startTime = LocalDateTime.now(zoneId)

        return new TaskDescription(next, offer)
    }

    boolean hasTask() {

        Task next

        if (tasksToBeProcessed.empty) {

            Optional<GroupContext> context = allGroupContexts.filter({ it.hasMoreTasksToProcess() }).findFirst()

            if (context.present) {
                return false
            }

            context = allGroupContexts.filter({ !it.running && !it.done }).findFirst()

            if (!context.present) {
                return false
            }

            next = context.get().taskContexts.first().task
            tasksToBeProcessed.add(next)

        } else {
            next = tasksToBeProcessed.peek()
        }

        return availableResources.hasCapacity(next)
    }

    void offerRescinded(String id) {
        availableResources.offerRescinded(id)
    }

    @Override
    Stream<TaskContext> getAllTasks() {
        return groupContexts.stream().flatMap({ it.taskContexts.stream() })
    }

    @Override
    Stream<TaskContext> getFinishedTaskContexts() {
        return groupContexts.stream().flatMap({ it.taskContexts.stream().filter({ it.done }) })
    }

    @Override
    Stream<GroupContext> getFinishedGroupContexts() {
        return groupContexts.stream().filter({ it.taskContexts.stream().allMatch({ it.done }) })
    }

    @Override
    int queueSize() {
        return tasksToBeProcessed.size()
    }

    private void killQueuedTask(TaskContext taskContext) {

        log.info(""" taskId="$taskContext.task.id" Going to remove task from queue. """)

        tasksToBeProcessed.remove(taskContext.task)
        taskContext.addEvent(new TaskEvent(StaticTaskStatus.KILLED))

        send(taskContext)
        send(getGroupContext(taskContext.task.id))
    }

    private void send(def payload) {

        switch (payload) {
            case GroupContext:
                groupContextChannel?.send(MessageBuilder.withPayload(payload).build())
                break
            case TaskContext:
                taskContextChannel?.send(MessageBuilder.withPayload(payload).build())
                break
            default:
                log.error("No channel for payload '$payload'! Cannot send update.")
        }
    }
}
