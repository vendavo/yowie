package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic
import groovy.transform.ToString

import javax.validation.constraints.NotNull

/**
 * Created by vtajzich
 */
@ToString
@CompileStatic
class PortMapping implements Comparable<PortMapping> {

    int containerPort
    int hostPort

    @NotNull
    Protocol protocol = Protocol.TCP

    @Override
    int compareTo(PortMapping rh) {

        if (rh == null) {
            return 1
        }

        return (hostPort as Integer).compareTo(rh.hostPort)
    }
}
