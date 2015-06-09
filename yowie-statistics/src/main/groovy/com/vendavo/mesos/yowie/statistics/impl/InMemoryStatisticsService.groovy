package com.vendavo.mesos.yowie.statistics.impl

import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.GroupContext
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.statistics.Statistics
import com.vendavo.mesos.yowie.statistics.StatisticsService
import groovy.transform.CompileStatic
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

import java.time.temporal.ChronoUnit

/**
 * Created by vtajzich
 */
@CompileStatic
class InMemoryStatisticsService implements StatisticsService {

    Map<String, List<Long>> taskStatisticsMap = [:]
    Map<String, List<Long>> groupStatisticsMap = [:]

    @Override
    void storeTaskStatistics(TaskContext context) {

        List values = taskStatisticsMap[context.task.type]

        if (values == null) {
            values = []
            taskStatisticsMap[context.task.type] = values
        }

        values << context.duration
    }

    @Override
    void storeGroupStatistics(GroupContext context) {

        long durationInMillis = context.duration

        List values = groupStatisticsMap[context.group.name]

        if (values == null) {
            values = []
            groupStatisticsMap[context.group.name] = values
        }

        values << durationInMillis
    }

    @Override
    Optional<Statistics> getStatisticsForTask(Task task) {
        return getStatistics { taskStatisticsMap[task.type] }
    }

    @Override
    Optional<Statistics> getStatisticsForGroup(Group group) {
        return getStatistics { groupStatisticsMap[group.type] }
    }

    private Optional<Statistics> getStatistics(Closure getDurations) {

        List<Long> durations = getDurations() as List<Long>

        if (durations == null) {
            return Optional.empty()
        }

        DescriptiveStatistics statistics = new DescriptiveStatistics()
        
        durations.each { statistics.addValue(it) }

        return Optional.of(new Statistics(statistics.mean, statistics.getPercentile(95), ChronoUnit.MILLIS))
    }
}
