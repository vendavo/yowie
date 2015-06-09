package com.vendavo.mesos.yowie.api.rest

import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.api.domain.TaskStatus
import com.vendavo.mesos.yowie.mesos.YowieFramework
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

/**
 * Created by vtajzich
 */
@CompileStatic
@RequestMapping('/tasks')
@RestController
class TaskController {

    @Autowired
    YowieFramework framework
    
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = '', method = RequestMethod.POST)
    Task create(@Valid @RequestBody Task task) {

        framework.createTask(task)

        return task
    }

    @RequestMapping(value = '/{id}/{status}', method = RequestMethod.PUT)
    TaskContext reachedStatus(@PathVariable('id') String id, @PathVariable('status') String status) {

        if (!id) {
            throw new IllegalArgumentException(""" Id cannot be null or empty! """)
        }

        TaskContext context = framework.getTaskContext(id)

        framework.taskHasReachedStatus(id, TaskStatus.createInstance(status))

        return context
    }
    
    @RequestMapping(value = '/{id}', method = RequestMethod.DELETE)
    TaskContext delete(@PathVariable('id') String id) {

        if (!id) {
            throw new IllegalArgumentException(""" Id cannot be null or empty! """)
        }
        
        TaskContext context = framework.getTaskContext(id)
        
        framework.kill(id)
        
        return context
    }

    @RequestMapping('/{id}')
    TaskContext getTaskStatus(@PathVariable('id') String id) {
        
        if (!id) {
            throw new IllegalArgumentException(""" Id cannot be null or empty! """)
        }
        
        return framework.getTaskContext(id)
    }

    @RequestMapping('')
    Collection<TaskContext> getAllTasks() {
        return framework.getAllTasks()
    }

    @RequestMapping('/status/finished')
    Collection<TaskContext> getFinishedTasks() {
        return framework.getFinishedTaskContexts()
    }
}
