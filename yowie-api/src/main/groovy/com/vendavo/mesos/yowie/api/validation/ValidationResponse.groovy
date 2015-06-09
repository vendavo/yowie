package com.vendavo.mesos.yowie.api.validation

import groovy.transform.CompileStatic
import groovy.transform.ToString

/**
 * Created by vtajzich
 */
@ToString
@CompileStatic
class ValidationResponse {
    
    List<ValidationResult> errors = []

    ValidationResponse(List<ValidationResult> errors) {
        this.errors = errors
    }
}
