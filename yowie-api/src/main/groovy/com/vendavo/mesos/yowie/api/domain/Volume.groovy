package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.constraints.NotNull

/**
 * Created by vtajzich
 */
@ToString
@CompileStatic
class Volume {

    @NotEmpty
    String containerPath
    
    @NotEmpty
    String hostPath
    
    @NotNull
    Mode mode = Mode.R
}
