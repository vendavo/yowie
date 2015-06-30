package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.api.domain.Constraint
import com.vendavo.mesos.yowie.api.domain.Container
import com.vendavo.mesos.yowie.api.domain.PortMapping
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.mesos.ResourceOffer
import com.vendavo.mesos.yowie.exception.NoResourcesAvailableException
import com.vendavo.mesos.yowie.mesos.builder.ResourcesAvailableBuilder
import spock.lang.Specification

/**
 * Created by vtajzich
 */
class ResourcesAvailableSpec extends Specification {

    ResourcesAvailable resources

    def setup() {

        resources = new ResourcesAvailableBuilder().withResource('1', 4, 4096, 1520..1550, 'name': 'resource-1').withResource('2', 2, 2048, 8080..8081, 'name': 'resource-2').build()
    }

    def "should allocate resources accordingly"() {

        given:

        Task task = new Task(cpus: cpus, mem: mem)

        when:

        ResourceOffer allocatedOn = resources.allocate(task)
        double cpusRemaiming = allocatedOn.cpus
        double memRemaiming = allocatedOn.mem

        then:

        expectedCpusRemaiming == cpusRemaiming
        expectedMemRemaiming == memRemaiming

        where:

        cpus | mem  | expectedCpusRemaiming | expectedMemRemaiming
        1    | 1024 | 1                     | 1024
        1    | 3072 | 3                     | 1024
        4    | 4096 | 0                     | 0
        2    | 2048 | 0                     | 0
    }

    def "should allocate resources according to ports available"() {

        given:

        Task task = new Task(cpus: 1, mem: 10, container: new Container(portMappings: [new PortMapping(hostPort: hostPort)]))

        when:

        ResourceOffer allocatedOn = resources.allocate(task)

        then:

        allocatedOn.id == expectedId

        where:

        hostPort | expectedId
        1520     | '1'
        1521     | '1'
        8080     | '2'
        8081     | '2'
    }

    def "should allocated correct resource"() {

        given:

        resources = new ResourcesAvailableBuilder()
                .withResource('1', 1, 1024, 'name': 'resource-1')
                .withResource('2', 1, 1024, 'name': 'resource-2')
                .withResource('3', 1, 1024, 'name': 'resource-3')
                .withResource('4', 1, 1024, 'name': 'resource-4')
                .build()

        Task task = new Task("task-$expectedId", 1, 64, [new Constraint('name', constraintValue)])

        when:

        ResourceOffer offer = resources.allocate(task)

        then:

        offer.id == expectedId

        where:

        expectedId | constraintValue
        '1'        | 'resource-1'
        '2'        | 'resource-2'
        '3'        | 'resource-3'
        '4'        | 'resource-4'
    }

    def "should remove rescinded offer"() {

        when:

        resources.offerRescinded(id)

        then:

        resources.offers.size() == 1
        resources.offers[0].offer.id.value == expected

        where:

        id  | expected
        '1' | '2'
        '2' | '1'
    }

    def "should throw an exception when no resources are available to be allocated"() {

        given:

        Task task = new Task(cpus: cpus, mem: mem)

        when:

        resources.allocate(task)

        then:

        thrown(NoResourcesAvailableException)

        where:

        cpus | mem
        5    | 1024
        4    | 4097
    }
}
