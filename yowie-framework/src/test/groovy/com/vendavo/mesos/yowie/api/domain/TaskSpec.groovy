package com.vendavo.mesos.yowie.api.domain

import spock.lang.Specification

/**
 * Created by vtajzich
 */
class TaskSpec extends Specification {
    
    def "tasks should not be equal"() {
    
        given:
        
        Task task1 = new Task()
        Task task2 = new Task()

        when:
        
        boolean equals = task1.equals(task2)
        
        then:
        
        task1.id != task2.id
        !task1.id.equals(task2.id)
        
        !equals
    }
}
