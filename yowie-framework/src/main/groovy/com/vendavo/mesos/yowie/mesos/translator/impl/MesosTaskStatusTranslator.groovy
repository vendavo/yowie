package com.vendavo.mesos.yowie.mesos.translator.impl

import com.vendavo.mesos.yowie.api.domain.StaticTaskStatus
import com.vendavo.mesos.yowie.api.domain.TaskStatus
import com.vendavo.mesos.yowie.mesos.translator.StatusTranslator
import groovy.transform.CompileStatic
import org.apache.mesos.Protos
import org.springframework.stereotype.Component

/**
 * Created by vtajzich
 */
@Component
@CompileStatic
class MesosTaskStatusTranslator implements StatusTranslator {

    @Override
    TaskStatus translate(Protos.TaskStatus status) {

        TaskStatus taskStatus

        switch (status.state) {

            case Protos.TaskState.TASK_STAGING:
                taskStatus = StaticTaskStatus.STAGING
                break
            case Protos.TaskState.TASK_RUNNING:
                taskStatus = StaticTaskStatus.RUNNING
                break
            case Protos.TaskState.TASK_FINISHED:
                taskStatus = StaticTaskStatus.FINISHED
                break
            case Protos.TaskState.TASK_LOST:
                taskStatus = StaticTaskStatus.LOST
                break
            case Protos.TaskState.TASK_KILLED:
                taskStatus = StaticTaskStatus.KILLED
                break
            default:
                taskStatus = StaticTaskStatus.ERROR
                break
        }
        
        return taskStatus
    }
}
