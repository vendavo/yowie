package com.vendavo.mesos.yowie.mesos

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.vendavo.mesos.yowie.event.DisconnectedEvent
import com.vendavo.mesos.yowie.event.ExecutorLostEvent
import com.vendavo.mesos.yowie.event.MesosErrorEvent
import com.vendavo.mesos.yowie.event.SlaveLostEvent
import org.apache.mesos.Protos
import org.apache.mesos.Scheduler
import org.apache.mesos.SchedulerDriver
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.event.ApplicationFailedEvent
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.context.event.ApplicationStartingEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.ContextStartedEvent
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

/**
 * Created by vtajzich
 */
class ConnectionManagerSpec extends Specification {

    private static String VALID_ID = 'yowie_id'

    static WireMockServer wireMockServer

    YowieFramework framework

    Protos.FrameworkInfo frameworkInfo

    Scheduler scheduler

    MesosDriverFactory driverFactory

    SchedulerDriver driver

    ConnectionManager manager

    def setupSpec() {

        wireMockServer = new WireMockServer(wireMockConfig())
        wireMockServer.start()
    }

    def cleanupSpec() {
        wireMockServer.stop()
    }

    def setup() {

        framework = Mock(YowieFramework)
        scheduler = Mock(Scheduler)
        driverFactory = Mock(MesosDriverFactory)
        driver = Mock(SchedulerDriver)

        frameworkInfo = Protos.FrameworkInfo.newBuilder().setName('test').setUser('a user').setPrincipal('a principal').build()

        manager = new ConnectionManager('localhost', '8080', framework, frameworkInfo, scheduler, driverFactory)

        WireMock.reset()
    }

    def "should check that framework IS registered and not re-create and not re-start driver"() {

        given:

        stubFor(get(urlPathEqualTo('/master/state.json')).willReturn(
                aResponse()
                        .withStatus(200)
                        .withHeader('Content-Type', 'application/json')
                        .withBody(CHECK_RESPONSE)
        ))

        when:

        manager.connect()
        manager.onResources(null)

        manager.check()

        then:

        1 * framework.id >> VALID_ID
        1 * driverFactory.createDriver(scheduler, frameworkInfo, !null) >> driver
        1 * driver.run()
    }

    def "should check that framework is NOT registered and re-create and re-start driver"() {

        given:

        stubFor(get(urlPathEqualTo('/master/state.json')).willReturn(
                aResponse()
                        .withStatus(200)
                        .withHeader('Content-Type', 'application/json')
                        .withBody(CHECK_RESPONSE)
        ))

        when:

        manager.connect()
        manager.onResources(null)

        manager.check()

        then:

        1 * framework.id >> 'some_other_id'
        2 * driverFactory.createDriver(scheduler, frameworkInfo, !null) >> driver
        2 * driver.run() >> Protos.Status.DRIVER_RUNNING
        1 * driver.stop()
    }

    def "should connect on application events"() {

        when:

        manager.onApplicationStarted(event)

        then:

        numOfCalls * driverFactory.createDriver(scheduler, frameworkInfo, !null) >> driver
        numOfCalls * driver.run()

        where:

        numOfCalls | event
        1          | new DisconnectedEvent('source')
        1          | new MesosErrorEvent('source')
        1          | new ApplicationStartingEvent(Mock(SpringApplication), null)
        1          | new ContextStartedEvent(Mock(ApplicationContext))
        1          | new ContextRefreshedEvent(Mock(ApplicationContext))
        0          | new ApplicationFailedEvent(Mock(SpringApplication), null, null, null)
    }

    private static String CHECK_RESPONSE = """
{
    "activated_slaves": 1,
    "build_date": "2015-01-09 02:42:30",
    "build_time": 1420771350,
    "build_user": "root",
    "cluster": "test_cluster",
    "completed_frameworks": [],
    "deactivated_slaves": 0,
    "elected_time": 1433836110.18719,
    "failed_tasks": 0,
    "finished_tasks": 0,
    "flags": {
        "allocation_interval": "1secs",
        "authenticate": "false",
        "authenticate_slaves": "false",
        "authenticators": "crammd5",
        "cluster": "test_cluster",
        "framework_sorter": "drf",
        "help": "false",
        "initialize_driver_logging": "true",
        "ip": "127.0.0.1",
        "log_auto_initialize": "true",
        "log_dir": "\\/var\\/log",
        "logbufsecs": "0",
        "logging_level": "INFO",
        "port": "5050",
        "quiet": "false",
        "quorum": "1",
        "recovery_slave_removal_limit": "100%",
        "registry": "replicated_log",
        "registry_fetch_timeout": "1mins",
        "registry_store_timeout": "5secs",
        "registry_strict": "false",
        "root_submissions": "true",
        "slave_reregister_timeout": "10mins",
        "user_sorter": "drf",
        "version": "false",
        "webui_dir": "\\/usr\\/share\\/mesos\\/webui",
        "whitelist": "*",
        "work_dir": "\\/var\\/lib\\/mesos",
        "zk": "file:\\/\\/etc\\/zk_hosts",
        "zk_session_timeout": "10secs"
    },
    "frameworks": [
        {
            "active": true,
            "checkpoint": false,
            "completed_tasks": [],
            "failover_timeout": 0,
            "hostname": "localhost",
            "id": "${VALID_ID}",
            "name": "Yowie",
            "offered_resources": {
                "cpus": 0,
                "disk": 0,
                "mem": 0
            },
            "offers": [],
            "registered_time": 1433836112.41592,
            "resources": {
                "cpus": 0,
                "disk": 0,
                "mem": 0
            },
            "role": "*",
            "tasks": [],
            "unregistered_time": 0,
            "used_resources": {
                "cpus": 0,
                "disk": 0,
                "mem": 0
            },
            "user": "root",
            "webui_url": ""
        }
    ],
    "git_sha": "2ae1ba91e64f92ec71d327e10e6ba9e8ad5477e8",
    "git_tag": "0.21.1",
    "hostname": "127.0.0.1",
    "id": "20150609-074830-1175086346-5050-19",
    "killed_tasks": 0,
    "leader": "master@127.0.0.1:5050",
    "log_dir": "\\/var\\/log",
    "lost_tasks": 0,
    "orphan_tasks": [],
    "pid": "master@127.0.0.1:5050",
    "slaves": [
        {
            "attributes": {
                "name": "mesos-slave-1"
            },
            "hostname": "127.0.0.1",
            "id": "20150528-082647-1175086346-5050-19-S0",
            "pid": "slave(1)@127.0.0.1:5051",
            "registered_time": 1433836111.16413,
            "reregistered_time": 1433836111.16414,
            "resources": {
                "cpus": 4,
                "disk": 229110,
                "mem": 14784,
                "ports": "[31000-32000]"
            }
        }
    ],
    "staged_tasks": 0,
    "start_time": 1433836110.16691,
    "started_tasks": 0,
    "unregistered_frameworks": [],
    "version": "0.21.1"
}
"""
}
