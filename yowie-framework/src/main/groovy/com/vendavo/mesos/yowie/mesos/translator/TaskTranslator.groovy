package com.vendavo.mesos.yowie.mesos.translator

import com.vendavo.mesos.yowie.mesos.TaskDescription
import org.apache.mesos.Protos

/**
 * Created by vtajzich
 */
interface TaskTranslator {

    Protos.TaskInfo translate(TaskDescription description)
}
