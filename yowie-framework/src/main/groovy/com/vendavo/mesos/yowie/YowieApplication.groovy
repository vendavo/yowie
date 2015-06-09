package com.vendavo.mesos.yowie

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.ImportResource
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar

@Slf4j
@CompileStatic
@ImportResource(['classpath:yowie-messaging-context.xml'])
@ComponentScan(basePackages = 'com.vendavo.mesos.yowie')
@EnableScheduling
@EnableAsync
@SpringBootApplication
class YowieApplication implements SchedulingConfigurer {

    @Qualifier('taskScheduler')
    @Autowired
    TaskScheduler taskScheduler
    
    static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(YowieApplication, args)
        context.registerShutdownHook()
    }

    @Override
    void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setTaskScheduler(taskScheduler)
    }
}
