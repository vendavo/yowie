package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.hibernate.validator.constraints.NotEmpty

/**
 * Created by vtajzich
 */
@ToString
@CompileStatic
class Parameter {

    @NotEmpty
    String key
    
    @NotEmpty
    String value
}
