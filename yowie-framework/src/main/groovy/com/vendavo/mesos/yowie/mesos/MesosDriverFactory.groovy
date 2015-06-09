package com.vendavo.mesos.yowie.mesos

import org.apache.mesos.Protos
import org.apache.mesos.Scheduler
import org.apache.mesos.SchedulerDriver

/**
 * Created by vtajzich
 */
interface MesosDriverFactory {

    SchedulerDriver createDriver(Scheduler scheduler, Protos.FrameworkInfo frameworkInfo, String url)
}
