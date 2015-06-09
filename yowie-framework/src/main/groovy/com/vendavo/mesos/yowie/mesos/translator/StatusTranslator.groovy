package com.vendavo.mesos.yowie.mesos.translator

import com.vendavo.mesos.yowie.api.domain.TaskStatus
import org.apache.mesos.Protos

/**
 * Created by vtajzich
 */
interface StatusTranslator {
    
    TaskStatus translate(Protos.TaskStatus status)
}
