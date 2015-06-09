package com.vendavo.mesos.yowie.statistics.es

import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.statistics.Statistics
import org.elasticsearch.bootstrap.Elasticsearch
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Created by vtajzich
 * 
 * I really wanted to use ES integration test support. However I'm not able to resolve this:
 * 
 * java.lang.AssertionError: fix your classpath to have tests-framework.jar before lucene-core.jar
 * 
 * Therefor I leave this test at least.
 *  
 */
@IgnoreIf({ !System.getenv().get('ES_URL') })
class ElasticSearchStatisticsServiceSpec extends Specification {

    ElasticSearchStatisticsService service

    def setup() {
        
        String esURL = System.getenv().get('ES_URL')

        service = new ElasticSearchStatisticsService(bulkActionSize: 1, bulkFlushInterval: 1, bulkSizeInMB: 1, bulkConcurrentRequests: 1, clusterName: 'benchmarks', hostName: esURL, port: 9300)
        service.afterPropertiesSet()

        service.deleteIndexIfExists()
        service.createIndexIfNotExists()
        
        Thread.sleep(2000)
    }

    def cleanup() {

        service.destroy()
    }

    def "should return correct statistics"() {

        given:

        LocalDateTime may21st10oClock = LocalDateTime.of(2015, 5, 21, 10, 0, 0, 0)

        TaskContext context1 = new TaskContext(new Task(name: 'task-1'))
        context1.setStartTime(may21st10oClock)
        context1.setEndTime(may21st10oClock.plusMinutes(11))


        when:

        service.storeTaskStatistics(context1)

        Thread.sleep(service.bulkFlushInterval + 3000)

        Optional<Statistics> statistics = service.getStatisticsForTask(context1.task)

        then:

        statistics.present

        statistics.get().getAverageTime(ChronoUnit.MINUTES).toMinutes() == 11
        statistics.get().getPercentil95Time(ChronoUnit.MINUTES).toMinutes() == 11
    }

    def "should not return statistics"() {

        given:

        TaskContext context1 = new TaskContext(new Task(name: 'task-1'))

        when:

        Optional<Statistics> statistics = service.getStatisticsForTask(context1.task)

        then:

        !statistics.present
    }
}
