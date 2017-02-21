package com.vendavo.mesos.yowie.mesos;

import com.vendavo.mesos.yowie.event.DisconnectedEvent;
import com.vendavo.mesos.yowie.event.ExecutorLostEvent;
import com.vendavo.mesos.yowie.event.MesosErrorEvent;
import com.vendavo.mesos.yowie.event.SlaveLostEvent;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by vtajzich
 */
@Component
public class ConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

    private String mesosUrl;

    private String mesosPort;

    private YowieFramework framework;

    private Protos.FrameworkInfo frameworkInfo;

    private Scheduler taskScheduler;

    private MesosDriverFactory driverFactory;

    private SchedulerDriver driver;

    private RestTemplate restTemplate = new RestTemplate();

    private Protos.Status status = Protos.Status.DRIVER_NOT_STARTED;

    private boolean statusCheckEnabled;
    private boolean driverStarted;

    private final String url; 

    @Autowired
    public ConnectionManager(@Value("${mesos.url}") String mesosUrl, @Value("${mesos.port}") String mesosPort, YowieFramework framework, Protos.FrameworkInfo frameworkInfo, Scheduler taskScheduler, MesosDriverFactory driverFactory) {
        this.mesosUrl = mesosUrl;
        this.mesosPort = mesosPort;
        this.framework = framework;
        this.frameworkInfo = frameworkInfo;
        this.taskScheduler = taskScheduler;
        this.driverFactory = driverFactory;
        
        this.url = MessageFormat.format("http://{0}:{1}/master/state.json", mesosUrl, mesosPort);
    }

    @Scheduled(fixedRate = 3000L)
    public void check() {

        if (!statusCheckEnabled || !driverStarted) {
            return;
        }

        try {

            Map response = restTemplate.getForObject(url, Hashtable.class);

            log.debug("Checking framework status on master node.");

            List<Map> frameworks = (List<Map>) response.get("frameworks");

            Optional<Map> current = frameworks.stream().filter(map -> map.get("id").equals(framework.getId())).findFirst();

            boolean registered = current.isPresent();

            log.debug("Framework is currently registered: {}", registered);

            if (!registered) {
                connect();
            }

        } catch (RestClientException ex) {
            //wait for next check
            log.warn(" Cannot connect to mesos master: {}", ex.getMessage(), ex);
        }
    }

    public void onResources(ResourcesAvailable resources) {
        driverStarted = true;
    }

    @Async
    public void onApplicationStarted(ApplicationEvent event) {

        if (event instanceof ContextRefreshedEvent ||
                event instanceof ApplicationStartingEvent ||
                event instanceof ContextStartedEvent ||
                event instanceof DisconnectedEvent ||
                event instanceof MesosErrorEvent) {
            connect();
        }
    }        

    public void connect() {
        switch (status) {

            case DRIVER_RUNNING:
                driver.stop();

            case DRIVER_NOT_STARTED:
            case DRIVER_STOPPED:
            case DRIVER_ABORTED:

                statusCheckEnabled = true;

                log.info("driverStatus=\"{}\" Going to run driver. ", status);

                driver = driverFactory.createDriver(taskScheduler, frameworkInfo, mesosUrl + ":" + mesosPort);
                status = driver.run();

                break;
        }
    }
}
