package com.vendavo.mesos.yowie.api.rest.response

import groovy.transform.CompileStatic

/**
 * Created by vtajzich
 */
@CompileStatic
class GenericError {
    
    String id = UUID.randomUUID().toString()
    String message
}
