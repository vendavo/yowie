package com.vendavo.mesos.yowie.api.validation

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Created by vtajzich
 */
@ToString
@CompileStatic
class ValidationResult {
    
    String field
    String message
    String code
    Object rejectedValue
    boolean bindingFailure

    ValidationResult(String field, String message, String code, Object rejectedValue, boolean bindingFailure) {
        this.field = field
        this.message = message
        this.code = code
        this.rejectedValue = rejectedValue
        this.bindingFailure = bindingFailure
    }
}
