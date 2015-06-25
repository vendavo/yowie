package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.GroupContext
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.api.domain.TaskStatus
import org.apache.mesos.SchedulerDriver

import java.util.stream.Stream

/**
 * Created by vtajzich
 */
interface YowieFramework {

    String getId()
    
    void initialize(SchedulerDriver driver, String id)

    void updateResources(ResourcesAvailable resources)

    GroupContext createTask(Group group)

    void kill(String taskId)
    
    void createTask(Task task)

    void taskHasReachedStatus(String id, TaskStatus status)
    
    TaskContext getTaskContext(String id)
    
    ResourcesAvailable getAvailableResources()
    
    Stream<GroupContext> getAllGroupContexts()

    Stream<TaskContext> getFinishedTaskContexts()

    Stream<GroupContext> getFinishedGroupContexts()

    TaskDescription getNextTask()
    
    boolean hasTask()

    void offerRescinded(String id)

    Stream<TaskContext> getAllTasks()
    
    int queueSize()
}
