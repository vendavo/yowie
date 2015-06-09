package com.vendavo.mesos.yowie.api.mesos

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vendavo.mesos.yowie.api.domain.Task
import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import org.apache.mesos.Protos

/**
 * Created by vtajzich
 */
@EqualsAndHashCode(includes = ['id'])
@CompileStatic
class ResourceOffer {

    @JsonIgnore
    Protos.Offer offer
    
    final String id
    
    double cpus
    double mem
    double disk
    
    Map<String, String> attributes = [:]

    @JsonIgnore
    Collection<Range<Integer>> ports

    ResourceOffer(Protos.Offer offer, double cpus, double mem, double disk, Collection<Range<Integer>> ports, Map<String, String> attributes) {
        this.offer = offer
        this.cpus = cpus
        this.mem = mem
        this.disk = disk
        this.ports = ports
        this.attributes = attributes
        this.id = offer?.getId()?.value
    }
    
    boolean hasCapacity(Task task) {
        
        boolean hasResources = cpus >= task.cpus && mem >= task.mem
        boolean constraintsSatisfied = true
        
        if (hasResources) {
        
            constraintsSatisfied = task.constraints.stream()
                    .filter({ attributes.get(it.name) != it.value })
                    .map({ false })
                    .findFirst().orElse(true)
        } 
        
        return hasResources && constraintsSatisfied
    }

    void allocate(Task task) {
        cpus -= task.cpus
        mem -= task.mem
    }
    
    List<String> getPortsDescription() {
        return ports.collect { "${it.toString()}" as String }
    }   

    @Override
    public String toString() {
        return """ id: $id, cpus: $cpus, mem: $mem, disk: $disk, ports: ${getPortsDescription()}, attributes: $attributes """
    }
}
