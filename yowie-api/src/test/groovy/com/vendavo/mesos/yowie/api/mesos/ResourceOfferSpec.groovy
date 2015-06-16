package com.vendavo.mesos.yowie.api.mesos

import com.vendavo.mesos.yowie.api.domain.Constraint
import com.vendavo.mesos.yowie.api.domain.Container
import com.vendavo.mesos.yowie.api.domain.PortMapping
import com.vendavo.mesos.yowie.api.domain.Task
import spock.lang.Specification

/**
 * Created by vtajzich
 */
class ResourceOfferSpec extends Specification {

    ResourceOffer offer

    void setup() {

        Range<Integer> port8080_8085 = 8080..8085
        Range<Integer> port9090_9091 = 9090..9091

        offer = new ResourceOffer(null, 10d, 4096d, 262144d, [port8080_8085, port9090_9091], [:])
    }

    def "should decide capacity request"() {

        given:

        PortMapping portMapping = new PortMapping(containerPort: 8080, hostPort: port)
        Container container = new Container(portMappings: [portMapping])

        Task task = new Task(cpus: cpus, mem: mem, container: container)

        when:

        boolean result = offer.hasCapacity(task)

        then:

        expected == result

        where:

        cpus | mem    | port | expected
        1    | 1024   | 8080 | true
        10   | 4096   | 8080 | true
        10.1 | 4096   | 8080 | false
        10   | 4096.1 | 8080 | false
        1    | 1024   | 8081 | true
        1    | 1024   | 8082 | true
        1    | 1024   | 8083 | true
        1    | 1024   | 8084 | true
        1    | 1024   | 8085 | true
        1    | 1024   | 9090 | true
        1    | 1024   | 9091 | true
        1    | 1024   | 9095 | false

    }

    def "should decide capacity request according to constraints"() {

        given:

        offer.attributes = attributes

        Task task = new Task(cpus: cpus, mem: mem, constraints: constraints)

        when:

        boolean result = offer.hasCapacity(task)

        then:

        expected == result

        where:

        cpus | mem | attributes                                     | constraints                                                                          | expected
        1    | 1   | [:]                                            | []                                                                                   | true
        1    | 1   | ['name': 'resource-1']                         | []                                                                                   | true
        1    | 1   | ['name': 'resource-1']                         | [new Constraint('name', 'resource-1')]                                               | true
        1    | 1   | ['name': 'resource-1', 'attr-2': 'some-value'] | [new Constraint('name', 'resource-1'), new Constraint('attr-2', 'some-value')]       | true
        1    | 1   | [:]                                            | [new Constraint('name', 'resource-1')]                                               | false
        1    | 1   | ['name': 'resource-1']                         | [new Constraint('name', 'resource-not-existing')]                                    | false
        1    | 1   | ['name': 'resource-1', 'attr-2': 'some-value'] | [new Constraint('name', 'resource-1'), new Constraint('attr-2', 'some-OTHER-value')] | false

    }

    def "should allocate resources accordingly"() {

        given:

        Task task = new Task(cpus: cpus, mem: mem)

        when:

        offer.allocate(task)
        double cpusRemaiming = offer.cpus
        double memRemaiming = offer.mem

        then:

        expectedCpusRemaiming == cpusRemaiming
        expectedMemRemaiming == memRemaiming

        where:

        cpus | mem  | expectedCpusRemaiming | expectedMemRemaiming
        1    | 1024 | 9                     | 3072
        10   | 4096 | 0                     | 0
        5.1  | 2044 | 4.9                   | 2052
    }
}
