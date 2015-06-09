package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.hibernate.validator.constraints.NotEmpty

/**
 * Created by vtajzich
 */
@EqualsAndHashCode
@CompileStatic
class Constraint {
    
    @NotEmpty
    String name
    
    @NotEmpty
    String value

    Constraint() {
    }

    Constraint(String name, String value) {
        this.name = name
        this.value = value
    }
}
