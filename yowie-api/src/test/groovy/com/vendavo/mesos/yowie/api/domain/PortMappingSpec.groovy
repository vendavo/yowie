package com.vendavo.mesos.yowie.api.domain

import spock.lang.Specification

/**
 * Created by vtajzich
 */
class PortMappingSpec extends Specification {

    PortMapping mapping

    def setup() {
        mapping = new PortMapping()
    }

    def "should compare correctly host port"() {

        given:

        mapping.hostPort = hostPort

        when:

        int result = mapping.compareTo(new PortMapping(hostPort: compareToHostPort))

        then:

        result == expected

        where:

        hostPort | compareToHostPort | expected
        0        | 0                 | 0
        10       | 5                 | 1
        5        | 10                | -1
        1        | 1                 | 0
    }
}
