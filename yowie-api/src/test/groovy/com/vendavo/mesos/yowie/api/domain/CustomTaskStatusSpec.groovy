package com.vendavo.mesos.yowie.api.domain

import spock.lang.Specification

/**
 * Created by vtajzich
 */
class CustomTaskStatusSpec extends Specification {

    def "should be equal when name matches"() {

        when:

        boolean equals = status1.equals(status2)

        then:

        equals == expected

        where:

        status1                                      | status2                                | expected
        new CustomTaskStatus('status_1')             | new CustomTaskStatus('status_1')       | true
        new CustomTaskStatus('status_1', true)       | new CustomTaskStatus('status_1')       | true
        new CustomTaskStatus('status_1')             | new CustomTaskStatus('status_1', true) | true
        new CustomTaskStatus('status_1', true, true) | new CustomTaskStatus('status_1')       | true
        new CustomTaskStatus('status_1_AAAA')        | new CustomTaskStatus('status_1')       | false
    }
}
