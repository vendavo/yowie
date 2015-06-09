package com.vendavo.mesos.yowie.mesos

import groovy.transform.CompileStatic
import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import java.time.ZoneId

/**
 * Created by vtajzich
 */
@CompileStatic
@Configuration
class MesosConfiguration {

    @Value('${mesos.url}')
    String mesosUrl

    @Value('${mesos.port}')
    String mesosPort
    
    @Value('${mesos.user}')
    String mesosUser
    
    @Value('${yowie.framework.name}')
    String name

    @Value('${yowie.framework.principal}')
    String principal
    
    @Bean
    MesosTaskScheduler mesosTaskScheduler() {
        return new MesosTaskScheduler()
    }
    
    @Bean
    Protos.FrameworkInfo frameworkInfo() {

        Protos.FrameworkInfo.Builder builder = Protos.FrameworkInfo.newBuilder()
        builder.setName(name)
                .setUser(mesosUser)
                .setPrincipal(principal)

        return builder.build()
    }
    
    @Bean
    ZoneId defaultZone() {
        return ZoneId.of('UTC')
    }
}
