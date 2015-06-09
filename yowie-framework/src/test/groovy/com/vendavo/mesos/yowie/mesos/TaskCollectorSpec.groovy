package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.mesos.builder.ResourceOfferBuilder
import com.vendavo.mesos.yowie.mesos.builder.ResourcesAvailableBuilder
import com.vendavo.mesos.yowie.mesos.translator.TaskTranslator
import org.apache.mesos.Protos
import spock.lang.Specification

import java.time.ZoneId

/**
 * Created by vtajzich
 */
class TaskCollectorSpec extends Specification {

    YowieFramework framework
    TaskTranslator taskTranslator

    TaskCollector collector

    def setup() {


        framework = Mock(YowieFramework)
        taskTranslator = Mock(TaskTranslator)

        collector = new TaskCollector(framework: framework, taskTranslator: taskTranslator, zoneId: ZoneId.of('UTC'))
    }

    def "should collect zero tasks and all offer ids as no task is available"() {

        given:

        ResourcesAvailableBuilder builder = new ResourcesAvailableBuilder()
        builder.withResource('1', 10, 10).withResource('2', 10, 10).withResource('3', 10, 10)

        ResourcesAvailable resourcesAvailable = builder.build()

        when:

        List<RunUnit> units = collector.collect()

        then:

        1 * framework.hasTask() >> false
        1 * framework.availableResources >> resourcesAvailable

        units.size() == 1
        units[0].tasks.size() == 0
        units[0].ids.size() == 3
    }

    def "should collect 1 task"() {

        given:

        ResourceOfferBuilder offerBuilder = new ResourceOfferBuilder()
        ResourceOffer resourceOffer = offerBuilder.build()

        ResourcesAvailableBuilder builder = new ResourcesAvailableBuilder()
        builder.withResource(resourceOffer)

        ResourcesAvailable resourcesAvailable = builder.build()

        when:

        List<RunUnit> units = collector.collect()

        then:

        2 * framework.hasTask() >>> [true, false]
        1 * framework.availableResources >> resourcesAvailable
        1 * framework.getTaskContext(_) >> new TaskContext(new Task())
        1 * framework.nextTask >> new TaskDescription(new Task(), resourceOffer)
        1 * taskTranslator.translate(_) >> { Protos.TaskInfo.newBuilder().buildPartial() }

        units.size() == 1
        units[0].tasks.size() == 1
        units[0].ids.size() == 1
    }

    def "should collect 1 task but 2 extra task units in order to able return not used resources"() {

        given:

        ResourceOffer resourceOffer1 = new ResourceOfferBuilder().withId('1').build()
        ResourceOffer resourceOffer2 = new ResourceOfferBuilder().withId('2').build()
        ResourceOffer resourceOffer3 = new ResourceOfferBuilder().withId('3').build()

        ResourcesAvailableBuilder builder = new ResourcesAvailableBuilder()
        builder.withResource(resourceOffer1).withResource(resourceOffer2).withResource(resourceOffer3)

        ResourcesAvailable resourcesAvailable = builder.build()

        when:

        List<RunUnit> units = collector.collect()

        then:

        2 * framework.hasTask() >>> [true, false]
        1 * framework.availableResources >> resourcesAvailable
        1 * framework.getTaskContext(_) >> new TaskContext(new Task())
        1 * framework.nextTask >> new TaskDescription(new Task(), resourceOffer1)
        1 * taskTranslator.translate(_) >> { Protos.TaskInfo.newBuilder().buildPartial() }

        units.size() == 2
        units[0].tasks.size() == 1
        units[0].ids.size() == 1
        units[0].ids[0].value == '1'

        units[1].tasks.size() == 0
        units[1].ids.size() == 2
    }

    def "should collect 3 tasks"() {

        given:

        ResourceOffer resourceOffer1 = new ResourceOfferBuilder().withId('1').build()
        ResourceOffer resourceOffer2 = new ResourceOfferBuilder().withId('2').build()
        ResourceOffer resourceOffer3 = new ResourceOfferBuilder().withId('3').build()

        ResourcesAvailableBuilder builder = new ResourcesAvailableBuilder()
        builder.withResource(resourceOffer1).withResource(resourceOffer2).withResource(resourceOffer3)

        ResourcesAvailable resourcesAvailable = builder.build()

        Task task1 = new Task()
        Task task2 = new Task()
        Task task3 = new Task()

        when:

        List<RunUnit> units = collector.collect()

        then:

        4 * framework.hasTask() >>> [true, true, true, false]
        1 * framework.availableResources >> resourcesAvailable
        3 * framework.getTaskContext(_) >>> [new TaskContext(task1), new TaskContext(task2), new TaskContext(task3)]
        3 * framework.nextTask >>> [new TaskDescription(task1, resourceOffer1),new TaskDescription(task2, resourceOffer2),new TaskDescription(task3, resourceOffer3)]
        3 * taskTranslator.translate(_) >> { Protos.TaskInfo.newBuilder().buildPartial() }

        units.size() == 3
        units[0].tasks.size() == 1
        units[0].ids.size() == 1
        units[0].ids[0].value == '1'

        units[1].tasks.size() == 1
        units[1].ids.size() == 1
        units[1].ids[0].value == '2'

        units[2].tasks.size() == 1
        units[2].ids.size() == 1
        units[2].ids[0].value == '3'
    }
}
