package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.StaticTaskStatus
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.api.domain.TaskEvent
import com.vendavo.mesos.yowie.mesos.translator.TaskTranslator
import groovy.transform.CompileStatic
import org.apache.mesos.Protos
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.stream.Collectors

/**
 * Created by vtajzich
 */
@Component
@CompileStatic
class TaskCollector {

    @Autowired
    TaskTranslator taskTranslator

    @Autowired
    YowieFramework framework

    @Autowired
    ZoneId zoneId

    List<RunUnit> collect() {

        List<TaskDescription> tasks = []

        while (framework.hasTask()) {

            TaskDescription taskDescription = framework.nextTask

            Task task = taskDescription.task

            TaskContext context = framework.getTaskContext(task.id)
            context.addEvent(new TaskEvent(LocalDateTime.now(zoneId), StaticTaskStatus.STARTED))

            tasks << taskDescription
        }

        List<RunUnit> units = tasks.groupBy { TaskDescription td -> td.resource }
                .entrySet()
                .collect { new RunUnit(it.key.offer.id, it.value.collect { taskTranslator.translate(it) }) }

        List<Protos.OfferID> notUsedOffers = framework.availableResources.offers.stream()
                .map({ it.offer.id })
                .filter({ Protos.OfferID offerID -> units.find { it.hasTaskFor(offerID) } == null })
                .collect(Collectors.toList())

        if (notUsedOffers) {
            units << new RunUnit(notUsedOffers)
        }

        return units
    }
}
