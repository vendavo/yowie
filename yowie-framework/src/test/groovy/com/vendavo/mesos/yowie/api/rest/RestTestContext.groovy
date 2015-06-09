package com.vendavo.mesos.yowie.api.rest

import com.vendavo.mesos.yowie.mesos.YowieFramework
import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

import static org.mockito.Mockito.mock

/**
 * Created by vtajzich
 */
@ComponentScan(basePackages = ['com.vendavo.mesos.yowie.api.rest'])
@SpringBootApplication
@CompileStatic
class RestTestContext {

    @Bean
    YowieFramework framework() {
        return mock(YowieFramework)
    }
}
