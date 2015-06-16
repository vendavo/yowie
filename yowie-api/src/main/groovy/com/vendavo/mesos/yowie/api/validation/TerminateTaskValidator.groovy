package com.vendavo.mesos.yowie.api.validation

import com.vendavo.mesos.yowie.api.domain.Group
import groovy.transform.CompileStatic

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Created by vtajzich
 */
@CompileStatic
class TerminateTaskValidator implements ConstraintValidator<ValidTerminateTask, Group> {

    @Override
    void initialize(ValidTerminateTask constraintAnnotation) {

    }

    @Override
    boolean isValid(Group value, ConstraintValidatorContext context) {

        if (!value || !value.terminateTask) {
            return true
        }

        return value.tasks.stream()
                .anyMatch({ it.name == value.terminateTask.name })
    }
}
