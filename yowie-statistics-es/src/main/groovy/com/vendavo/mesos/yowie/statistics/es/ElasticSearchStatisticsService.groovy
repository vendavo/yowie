package com.vendavo.mesos.yowie.statistics.es

import com.fasterxml.jackson.databind.ObjectMapper
import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.GroupContext
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.statistics.Statistics
import com.vendavo.mesos.yowie.statistics.StatisticsService
import groovy.transform.CompileStatic
import org.codehaus.groovy.binding.AggregateBinding
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
import org.elasticsearch.action.bulk.BulkProcessor
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.bulk.BulkResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.unit.ByteSizeUnit
import org.elasticsearch.common.unit.ByteSizeValue
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.metrics.percentiles.Percentiles
import org.elasticsearch.search.aggregations.metrics.stats.Stats
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.InitializingBean

import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * Created by vtajzich
 */
@CompileStatic
class ElasticSearchStatisticsService implements StatisticsService, InitializingBean, DisposableBean {

    public static final String TASK_TYPE = 'taskStatistics'
    public static final String INDEX = 'yowie'
    public static final String GROUP_TYPE = 'groupStatistics'
    
    Client client
    BulkProcessor bulkProcessor
    
    int bulkActionSize
    int bulkSizeInMB
    int bulkFlushInterval
    int bulkConcurrentRequests
    
    String clusterName
    
    String hostName
    int port

    ObjectMapper mapper = new ObjectMapper()

    @Override
    void storeTaskStatistics(TaskContext context) {

        byte[] json = mapper.writeValueAsBytes(context)
        bulkProcessor.add(new IndexRequest(INDEX, TASK_TYPE, context.task.id).source(json))
    }

    @Override
    void storeGroupStatistics(GroupContext context) {

        byte[] json = mapper.writeValueAsBytes(context)
        bulkProcessor.add(new IndexRequest(INDEX, GROUP_TYPE, context.group.id).source(json))
    }

    @Override
    Optional<Statistics> getStatisticsForTask(Task task) {

        SearchResponse sr = client.prepareSearch(INDEX)
                .setTypes(TASK_TYPE)
                .setQuery(QueryBuilders.termQuery("task.type", task.type))
                .addAggregation(AggregationBuilders.stats('stats').field('duration'))
                .addAggregation(AggregationBuilders.percentiles('percentiles').field('duration').percentiles(95d))
                .execute()
                .actionGet()
        
        Stats stats = sr.aggregations.get('stats') as Stats
        Percentiles percentiles = sr.aggregations.get('percentiles') as Percentiles

        Double avg = stats.avg
        Double percentile = percentiles.percentile(95)

        if (avg == Double.NaN || percentile == Double.NaN) {
            return Optional.empty()
        }
        
        Statistics statistics = new Statistics(avg, percentile, ChronoUnit.MILLIS)
        
        return Optional.of(statistics)
    }

    @Override
    Optional<Statistics> getStatisticsForGroup(Group group) {
        return null
    }

    @Override
    void afterPropertiesSet() throws Exception {

        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build()

        client = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(hostName, port))
        bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {

            @Override
            void beforeBulk(long executionId, BulkRequest request) {

            }

            @Override
            void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

            }

            @Override
            void afterBulk(long executionId, BulkRequest request, Throwable failure) {

            }
        })
                .setBulkActions(bulkActionSize)
                .setBulkSize(new ByteSizeValue(bulkFlushInterval, ByteSizeUnit.MB))
                .setFlushInterval(TimeValue.timeValueSeconds(bulkFlushInterval))
                .setConcurrentRequests(bulkConcurrentRequests)
                .build()
        
        createIndexIfNotExists()
    }

    void createIndexIfNotExists() {

        IndicesExistsResponse response = client.admin().indices().exists(new IndicesExistsRequest(INDEX)).actionGet()

        if (!response.exists) {

            String taskMapping = """
{
      "properties": {
        "startTime"  : { "type" : "date", "store" : "yes"},
        "endTime"  : { "type" : "date", "store" : "yes"},
        "duration" : { "type": "long" },
        "task.name"  : { "type" : "string", "index" : "not_analyzed", "store" : "yes" },
        "task.type"  : { "type" : "string", "index" : "not_analyzed", "store" : "yes" },
        "task.cpus"  : { "type" : "double", "store" : "yes" },
        "task.mem"  : { "type" : "double", "store" : "yes" },
        "task.version"  : { "type" : "string", "index" : "not_analyzed", "store" : "yes" },
        "done" : { "type" : "boolean" },
        "lastStatus" : { "type" : "string", "index" : "not_analyzed", "store" : "yes" }
      }
 }"""

            String groupMapping = """
{
      "properties": {
        "group.name"  : { "type" : "string", "index" : "not_analyzed", "store" : "yes" },
        "group.type"  : { "type" : "string", "index" : "not_analyzed", "store" : "yes" },
        "duration" : { "type": "long" },
        "done" : { "type" : "boolean" }
      }
 }"""

            CreateIndexRequest request = new CreateIndexRequest(INDEX)
            request.mapping(TASK_TYPE, taskMapping)
            request.mapping(GROUP_TYPE, groupMapping)

            CreateIndexResponse createIndexResponse = client.admin().indices().create(request).actionGet()

            if (!createIndexResponse.acknowledged) {
                println "ajaj"
            }
        }
    }


    void deleteIndexIfExists() {

        IndicesExistsResponse response = client.admin().indices().exists(new IndicesExistsRequest(INDEX)).actionGet()

        if (response.exists) {
            client.admin().indices().delete(new DeleteIndexRequest(INDEX)).actionGet()
        }
    }

    @Override
    void destroy() throws Exception {

        bulkProcessor.awaitClose(2, TimeUnit.MINUTES)
        client.close()
    }
}
