package com.vendavo.mesos.yowie.mesos

import groovy.transform.CompileStatic
import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos
import org.apache.mesos.Scheduler
import org.apache.mesos.SchedulerDriver
import org.springframework.stereotype.Component

/**
 * Created by vtajzich
 */
@Component
@CompileStatic
class MesosSchedulerDriverFactory implements MesosDriverFactory {

    @Override
    SchedulerDriver createDriver(Scheduler taskScheduler, Protos.FrameworkInfo frameworkInfo, String url) {
        new MesosSchedulerDriver(taskScheduler, frameworkInfo, url)
    }
}
