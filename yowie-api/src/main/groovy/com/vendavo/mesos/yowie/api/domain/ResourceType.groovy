package com.vendavo.mesos.yowie.api.domain

/**
 * Created by vtajzich
 */
enum ResourceType {

    CPUS('cpus'), MEM('mem'), PORTS('ports')

    final String value
    
    ResourceType(String value) {
        this.value = value
    }
}
