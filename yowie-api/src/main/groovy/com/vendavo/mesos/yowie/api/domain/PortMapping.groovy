package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic
import groovy.transform.ToString

import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

/**
 * Created by vtajzich
 */
@ToString
@CompileStatic
class PortMapping {

    int containerPort
    int hostPort

    @NotNull
    Protocol protocol = Protocol.TCP
}
