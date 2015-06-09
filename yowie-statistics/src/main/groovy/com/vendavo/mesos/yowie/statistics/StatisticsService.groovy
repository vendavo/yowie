package com.vendavo.mesos.yowie.statistics

import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.GroupContext
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext

/**
 * Created by vtajzich
 */
interface StatisticsService {

    void storeTaskStatistics(TaskContext context)
    
    void storeGroupStatistics(GroupContext context)
    
    Optional<Statistics> getStatisticsForTask(Task task)

    Optional<Statistics> getStatisticsForGroup(Group group)
}
