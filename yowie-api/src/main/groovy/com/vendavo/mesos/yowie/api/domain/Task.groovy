package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import javax.validation.Valid
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.NotNull

/**
 * Created by vtajzich
 */
@ToString
@EqualsAndHashCode(includes = ['id'])
@CompileStatic
class Task {

    final String id = UUID.randomUUID().toString()
    
    @NotNull
    String name
    
    String type
    
    @DecimalMin('0.1')
    double cpus

    @DecimalMin('0.1')
    double mem

    @Valid
    @NotNull
    Container container
    
    Map<String, String> env = [:]
    
    String version
    
    @Valid
    Dependency dependsOn
    
    @Valid
    List<Constraint> constraints = []

    Task() {
    }

    Task(String name, double cpus, double mem, List<Constraint> constraints) {
        this.name = name
        this.cpus = cpus
        this.mem = mem
        this.constraints = constraints
    }

    Task addConstraint(String name, String value) {
        
        constraints << new Constraint(name, value)
        
        return this
    }

    String getType() {
        return type ?: name
    }
}
