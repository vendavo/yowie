package com.vendavo.mesos.yowie.statistics.es;

import com.vendavo.mesos.yowie.api.domain.Task;
import com.vendavo.mesos.yowie.api.domain.TaskContext;
import com.vendavo.mesos.yowie.statistics.Statistics;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static junit.framework.TestCase.*;

/**
 * Created by vtajzich
 */
@Ignore
@ElasticsearchIntegrationTest.ClusterScope(scope = ElasticsearchIntegrationTest.Scope.TEST)
public class ElasticSearchStatisticsServiceTest extends ElasticsearchIntegrationTest {

    ElasticSearchStatisticsService service;

    @Before
    public void setup() throws Exception {

        service = new ElasticSearchStatisticsService();
        service.setBulkActionSize(1);
        service.setBulkFlushInterval(1);
        service.setBulkSizeInMB(1);
        service.setBulkConcurrentRequests(1);
        service.setClusterName("benchmarks");
        service.setHostName("localhost");
        service.setPort(9300);

//        service.afterPropertiesSet();
//
//        service.deleteIndexIfExists();
//        service.createIndexIfNotExists();

//        refresh();
    }

    @After
    public void cleanup() throws Exception{
        service.destroy();
    }

    @Test
    public void testCorrectStatistics() throws InterruptedException {

        LocalDateTime may21st10oClock = LocalDateTime.of(2015, 5, 21, 10, 0, 0, 0);

        Task task1 = new Task();
        task1.setName("task-1");


        TaskContext context1 = new TaskContext(task1);
        context1.setStartTime(may21st10oClock);
        context1.setEndTime(may21st10oClock.plusMinutes(11));

        service.storeTaskStatistics(context1);

        Thread.sleep(service.getBulkFlushInterval() + 3000);

        Optional<Statistics> statistics = service.getStatisticsForTask(task1);

        assertTrue(statistics.isPresent());

        assertEquals(11L, statistics.get().getAverageTime(ChronoUnit.MINUTES).toMinutes());
        assertEquals(11L, statistics.get().getPercentil95Time(ChronoUnit.MINUTES).toMinutes());
    }

    @Test
    public void testNotReturnStatistics() {

        Task task = new Task();
        task.setName("task-1");

        Optional<Statistics> statistics = service.getStatisticsForTask(task);

        assertFalse(statistics.isPresent());
    }
}
