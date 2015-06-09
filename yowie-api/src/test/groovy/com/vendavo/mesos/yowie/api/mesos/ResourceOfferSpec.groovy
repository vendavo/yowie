package com.vendavo.mesos.yowie.api.mesos

import com.vendavo.mesos.yowie.api.domain.Constraint
import com.vendavo.mesos.yowie.api.domain.Task
import spock.lang.Specification

/**
 * Created by vtajzich
 */
class ResourceOfferSpec extends Specification {

    ResourceOffer offer

    void setup() {
        offer = new ResourceOffer(null, 10d, 4096d, 262144d, [], [:])
    }

    def "should decide capacity request"() {

        given:

        Task task = new Task(cpus: cpus, mem: mem)

        when:

        boolean result = offer.hasCapacity(task)

        then:

        expected == result

        where:

        cpus | mem    | expected
        1    | 1024   | true
        10   | 4096   | true
        10.1 | 4096   | false
        10   | 4096.1 | false

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
