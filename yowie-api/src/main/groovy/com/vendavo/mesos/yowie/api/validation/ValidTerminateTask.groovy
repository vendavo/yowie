package com.vendavo.mesos.yowie.api.validation

import javax.validation.Constraint
import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by vtajzich
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER])
@Constraint(validatedBy = TerminateTaskValidator)
@interface ValidTerminateTask {

    String message() default 'Termination task is not valid'

    Class[] groups() default []

    Class[] payload() default []
}
