package com.vendavo.mesos.yowie.api.domain

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * Created by vtajzich
 */
@EqualsAndHashCode(includes = ['group'])
@CompileStatic
class GroupContext {

    @JsonIgnore
    final LocalDateTime created = LocalDateTime.now()
    final Group group
    Task current

    private Collection<TaskContext> taskContexts = []

    GroupContext(Group group) {
        this.group = group
        this.current = group.tasks.first()
        taskContexts = group.tasks.collect { new TaskContext(it) }
    }

    TaskContext getTaskContext(String id) {
        return taskContexts.find { it.task.id == id }
    }

    Stream<TaskContext> getNext(String taskId, TaskStatus status) {

        String name = taskContexts.find { it.task.id == taskId }?.task?.name

        return taskContexts.stream()
                .filter({ it.task?.dependsOn?.name == name && it.task?.dependsOn?.status == status })
                .filter({ it != null })
    }

    /**
     * @return TRUE if there is at least one task which didn't started yet
     */
    boolean hasMoreTasksToProcess() {
        return isRunning() && taskContexts.stream().anyMatch({ !it.started })
    }

    /**
     * @return TRUE if at least one task is running
     */
    boolean isRunning() {
        return taskContexts.stream().anyMatch({ it.running })
    }

    /**
     * @return TRUE if all tasks are done
     */
    boolean isDone() {
        return taskContexts.stream().allMatch({ it.done })
    }

    boolean isError() {
        return taskContexts.stream().anyMatch({ it.error })
    }

    /**
     *
     * @param context task context which has reached a status
     * @param status status
     * @return TRUE if terminate definition equals to given task and status
     */
    boolean shouldFinish(TaskContext context, TaskStatus status) {
        return group.terminateTask?.equals(new Dependency(context.task.name, status))
    }

    /**
     * @return read-only collection
     */
    Stream<TaskContext> getTaskContexts() {
        return taskContexts.stream()
    }
    
    @JsonProperty('taskContexts')
    List<TaskContext> getTaskContextsJson() {
        return getTaskContexts().collect(Collectors.toList())
    }

    /**
     *
     * It's not sum duration on tasks as there might be tasks which are run in parallel. 
     * So it's needed to calculate duration between two times.
     *
     * @return duration between start time and end time.
     */
    long getDuration() {

        if (!startTime || !endTime) {
            return 0
        }

        return startTime.until(endTime, ChronoUnit.MILLIS)
    }

    @JsonGetter('startTime')
    String getStartTimeJson() {
        return startTime ? DateTimeFormatter.ISO_DATE_TIME.format(startTime) : ''
    }

    @JsonGetter('endTime')
    String getEndTimeJson() {
        return endTime ? DateTimeFormatter.ISO_DATE_TIME.format(endTime) : ''
    }

    //TODO: use optional
    LocalDateTime getStartTime() {
        return taskContexts.stream()
                .filter({ it.startTime != null })
                .map({ it.startTime })
                .sorted()
                .findFirst().orElse(null)
    }

    //TODO: use optional
    LocalDateTime getEndTime() {
        return taskContexts.stream()
                .filter({ it.endTime != null })
                .map({ it.endTime })
                .sorted({ LocalDateTime o1, LocalDateTime o2 -> o2.compareTo(o1) } as Comparator)
                .findFirst().orElse(null)
    }
}
