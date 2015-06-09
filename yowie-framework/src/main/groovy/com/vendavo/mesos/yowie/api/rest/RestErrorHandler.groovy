package com.vendavo.mesos.yowie.api.rest

import com.vendavo.mesos.yowie.api.rest.response.GenericError
import com.vendavo.mesos.yowie.api.validation.ValidationResult
import com.vendavo.mesos.yowie.api.validation.ValidationResponse
import com.vendavo.mesos.yowie.exception.NoSuchTaskException
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Created by vtajzich
 */
@Slf4j
@ControllerAdvice
@CompileStatic
class RestErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationResponse processValidationError(MethodArgumentNotValidException ex) {

        BindingResult result = ex.getBindingResult()

        List<ValidationResult> validations =  result.allErrors.collect {
            new ValidationResult(it.objectName, it.defaultMessage, it.code, null, false)
        }

        def response = new ValidationResponse(validations)
        
        log.error(""" Bad request. Validation response: '$response' """)
        
        return response
    }

    @ExceptionHandler([NoSuchTaskException, IllegalArgumentException])
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public GenericError noSuchTask(Exception ex) {

        log.error(""" Bad request.""", ex)

        return new GenericError(message: ex.message)
    }

    @ExceptionHandler(Exception)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public GenericError anyError(Exception ex) {

        def error = new GenericError(message: "Something bad happened! :'(")
        
        log.error(""" errorId="$error.id" Exception during request processing. """, ex)

        return error
    }
}
