package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.TaskStatus
import com.vendavo.mesos.yowie.event.DisconnectedEvent
import com.vendavo.mesos.yowie.event.ExecutorLostEvent
import com.vendavo.mesos.yowie.event.MesosErrorEvent
import com.vendavo.mesos.yowie.event.SlaveLostEvent
import com.vendavo.mesos.yowie.mesos.translator.OffersTranslator
import com.vendavo.mesos.yowie.mesos.translator.StatusTranslator
import com.vendavo.mesos.yowie.mesos.translator.TaskTranslator
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.mesos.Protos
import org.apache.mesos.Scheduler
import org.apache.mesos.SchedulerDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.integration.support.MessageBuilder
import org.springframework.messaging.MessageChannel

import java.time.ZoneId

/**
 * Created by vtajzich
 */
@CompileStatic
@Slf4j
class MesosTaskScheduler implements Scheduler {

    @Autowired
    TaskTranslator taskTranslator

    @Autowired
    OffersTranslator offersTranslator

    @Autowired
    StatusTranslator statusTranslator

    @Autowired
    TaskCollector taskCollector

    @Autowired
    ZoneId zoneId

    @Autowired
    YowieFramework framework

    @Qualifier('resources')
    @Autowired
    MessageChannel resourcesChannel

    @Qualifier('mesosEventChannel')
    @Autowired
    MessageChannel eventChannel

    @Override
    void registered(SchedulerDriver driver, Protos.FrameworkID frameworkId, Protos.MasterInfo masterInfo) {

        log.info(""" driver="$driver" frameworkId="$frameworkId.value" Framework registered. """)

        framework.initialize(driver, frameworkId.value)
    }

    @Override
    void reregistered(SchedulerDriver driver, Protos.MasterInfo masterInfo) {
        log.info(""" driver="$driver" Framework re-registered. """)
    }

    @Override
    void resourceOffers(SchedulerDriver driver, List<Protos.Offer> offers) {

        notifyResourcesChange(offers)

        log.info(""" Resources offered: $framework.availableResources """)

        ResourcesAvailable resourcesAvailable = framework.availableResources

        taskCollector.collect().each { RunUnit ru ->

            String taskIds = ru.tasks.collect { it.name }.join(',')

            def resources = resourcesAvailable.offers
                    .findAll { ru.ids.contains(it.offer.id) }
                    .collect { " $it.id ($it.attributes)" }
                    .join(',')

            if (ru.tasks) {
                log.info(""" Launching taskIds="$taskIds" on resources="$resources" """)
            }

            driver.launchTasks(ru.ids, ru.tasks, ru.filters)
        }
    }

    @Override
    void offerRescinded(SchedulerDriver driver, Protos.OfferID offerId) {

        log.info(""" offerId="$offerId.value" Resource offer rescinded. """)

        framework.offerRescinded(offerId.value)
    }

    @Override
    void statusUpdate(SchedulerDriver driver, Protos.TaskStatus status) {

        String taskId = status.taskId.value.split('_').first()

        log.info(""" taskId="$taskId" mesosStatus="$status.state" Task status update. """)

        TaskStatus taskStatus = statusTranslator.translate(status)

        if (taskStatus) {
            framework.taskHasReachedStatus(taskId, taskStatus)
        }
    }

    @Override
    void frameworkMessage(SchedulerDriver driver, Protos.ExecutorID executorId, Protos.SlaveID slaveId, byte[] data) {
        log.info(""" driver="$driver" executorId="$executorId.value" slaveId="$slaveId.value" Received framework message: "${
            new String(data)
        }" """)
    }

    @Override
    void disconnected(SchedulerDriver driver) {

        log.info("Disconnected")

        eventChannel.send(MessageBuilder.withPayload(new DisconnectedEvent('')).build())
    }

    @Override
    void slaveLost(SchedulerDriver driver, Protos.SlaveID slaveId) {

        log.info(""" slaveId="$slaveId.value" Slave lost """)

        eventChannel.send(MessageBuilder.withPayload(new SlaveLostEvent(slaveId)).build())
    }

    @Override
    void executorLost(SchedulerDriver driver, Protos.ExecutorID executorId, Protos.SlaveID slaveId, int status) {

        log.info(""" executorId="$executorId" slaveId="$slaveId" Executor lost. """)

        eventChannel.send(MessageBuilder.withPayload(new ExecutorLostEvent(executorId)).build())
    }

    @Override
    void error(SchedulerDriver driver, String message) {

        log.error(""" driver="$driver" $message""")

        eventChannel.send(MessageBuilder.withPayload(new MesosErrorEvent(message)).build())
    }

    private void notifyResourcesChange(List<Protos.Offer> offers) {

        ResourcesAvailable resources = offersTranslator.translate(offers)

        framework.updateResources(resources)

        resourcesChannel.send(MessageBuilder.withPayload(resources).build())
    }
}
