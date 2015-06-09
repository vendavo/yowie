package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import groovy.transform.CompileStatic

/**
 * Created by vtajzich
 */
@CompileStatic
class TaskDescription {
    
    Task task
    ResourceOffer resource

    TaskDescription(Task task, ResourceOffer resource) {
        this.task = task
        this.resource = resource
    }
}
