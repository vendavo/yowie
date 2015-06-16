package com.vendavo.mesos.yowie.api.validation

import com.vendavo.mesos.yowie.api.domain.Task
import groovy.transform.CompileStatic

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

/**
 * Created by vtajzich
 */
@CompileStatic
class DependencyValidator implements ConstraintValidator<ValidDependencies, List<Task>> {

    @Override
    void initialize(ValidDependencies constraintAnnotation) {

    }

    @Override
    boolean isValid(List<Task> value, ConstraintValidatorContext context) {

        if (!value) {
            return true
        }

        List<String> taskNames = value.collect { it.name }

        return value.stream()
                .filter({ it.dependsOn != null })
                .map({ it.dependsOn })
                .allMatch({ taskNames.contains(it.name) })
    }
}
