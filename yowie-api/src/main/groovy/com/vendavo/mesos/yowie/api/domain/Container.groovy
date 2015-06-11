package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic
import groovy.transform.ToString
import org.hibernate.validator.constraints.NotEmpty

import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * Created by vtajzich
 */
@ToString
@CompileStatic
class Container {
    
    @NotEmpty
    String image
    
    @NotNull
    Network network = Network.NONE
    
    boolean privileged
    
    boolean forcePull
    
    @Valid
    List<Parameter> parameters = []
    
    @Valid
    List<PortMapping> portMappings = []
    
    @Valid
    List<Volume> volumes = []
}
