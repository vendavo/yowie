package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.constraints.NotNull

/**
 * Created by vtajzich
 */
@EqualsAndHashCode
@ToString
@CompileStatic
class Dependency {
    
    @NotEmpty
    String name
    
    @NotNull
    TaskStatus status

    Dependency() {
    }

    Dependency(String name, TaskStatus status) {
        this.name = name
        this.status = status
    }
}
