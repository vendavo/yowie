package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic

/**
 * Created by vtajzich
 */
@CompileStatic
class TaskBuilder {


    private Map<String, String> env
    private List<PortMapping> portMappings = []
    private Network network = Network.NONE
    

    TaskBuilder withEnvVariables(Map<String, String> env) {
        this.env = env
        return this
    }

    TaskBuilder withPort(int containerPort, int hostPort) {
        portMappings << new PortMapping(hostPort: hostPort, containerPort: containerPort)
        return this
    }
    
    TaskBuilder withNetwork(Network network) {
        this.network = network
        return this
    }
    
    Task build() {
        
        Task task = new Task()
        task.env = env
        task.container = new Container(portMappings: portMappings, image: 'image', network: network)
        
        return task
    }
}
