package com.vendavo.mesos.yowie.api.domain

import groovy.transform.CompileStatic

/**
 * Created by vtajzich
 */
@CompileStatic
enum Network {

    NONE, BRIDGE, HOST, CONTAINER, MANAGING
}
