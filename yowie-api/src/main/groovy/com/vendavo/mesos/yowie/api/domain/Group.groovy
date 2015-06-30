package com.vendavo.mesos.yowie.api.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.vendavo.mesos.yowie.api.validation.ValidDependencies
import com.vendavo.mesos.yowie.api.validation.ValidTerminateTask
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.Valid

/**
 * Created by vtajzich
 */
@ValidTerminateTask
@EqualsAndHashCode(includes = ['id'])
@CompileStatic
class Group {
    
    final String id = UUID.randomUUID().toString()
    
    @NotEmpty
    String name
    
    String type
    
    @Valid
    Dependency terminateTask

    @ValidDependencies
    private List<Task> tasks = []
    
    Group addTask(Task task) {
        tasks.add(task)
        return this
    }

    String getType() {
        return type ?: name
    }

    @JsonIgnore
    List<Task> getTasks() {
        return tasks
    }

    @JsonProperty('tasks')
    void setTasks(List<Task> tasks) {
        this.tasks = tasks
    }
}
