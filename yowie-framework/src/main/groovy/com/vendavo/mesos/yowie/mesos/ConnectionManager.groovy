package com.vendavo.mesos.yowie.mesos

import com.vendavo.mesos.yowie.event.DisconnectedEvent
import com.vendavo.mesos.yowie.event.ExecutorLostEvent
import com.vendavo.mesos.yowie.event.MesosErrorEvent
import com.vendavo.mesos.yowie.event.SlaveLostEvent
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.mesos.Protos
import org.apache.mesos.Scheduler
import org.apache.mesos.SchedulerDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.ContextStartedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

/**
 * Created by vtajzich
 */
@Slf4j
@Component
@CompileStatic
class ConnectionManager {

    @Value('${mesos.url}')
    String mesosUrl

    @Value('${mesos.port}')
    String mesosPort

    @Autowired
    YowieFramework framework

    @Autowired
    Protos.FrameworkInfo frameworkInfo

    @Autowired
    Scheduler taskScheduler

    @Autowired
    MesosDriverFactory driverFactory

    SchedulerDriver driver

    RestTemplate restTemplate = new RestTemplate()

    Protos.Status status = Protos.Status.DRIVER_NOT_STARTED

    private boolean statusCheckEnabled
    private boolean driverStarted

    @Scheduled(fixedRate = 3000L)
    void check() {

        if (!statusCheckEnabled || !driverStarted) {
            return
        }

        try {

            Map response = restTemplate.getForObject("http://$mesosUrl:$mesosPort/master/state.json", Hashtable) as Map

            log.debug(""" Checking framework status on master node. """)

            List<Map> frameworks = response.frameworks as List<Map>

            Map current = frameworks.find { it['id'] == framework.id }

            boolean registered = current != null

            log.debug(""" Framework registered $registered """)

            if (!registered) {
                connect()
            }

        } catch (RestClientException | ResourceAccessException ex) {
            //wait for next check
            log.warn(""" Cannot connect to mesos master: ${ex.message}""", ex)
        }
    }

    void onResources(ResourcesAvailable resources) {
        driverStarted = true
    }

    @Async
    void onApplicationStarted(ApplicationEvent event) {

        switch (event) {

            case ContextRefreshedEvent:
            case ApplicationStartedEvent:
            case ContextStartedEvent:
            case DisconnectedEvent:
            case ExecutorLostEvent:
            case MesosErrorEvent:
            case SlaveLostEvent:
                connect()
                break
        }
    }

    @Async
    void connect() {
        switch (status) {

            case Protos.Status.DRIVER_RUNNING:
                driver.stop()

            case Protos.Status.DRIVER_NOT_STARTED:
            case Protos.Status.DRIVER_STOPPED:
            case Protos.Status.DRIVER_ABORTED:

                statusCheckEnabled = true
                
                log.info(""" driverStatus="$status" Going to run driver. """)
      
                driver = driverFactory.createDriver(taskScheduler, frameworkInfo, "$mesosUrl:$mesosPort")
                status = driver.run()

                break
        }
    }
}
