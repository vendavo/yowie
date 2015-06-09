package com.vendavo.mesos.yowie.statistics.impl

import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.GroupContext
import com.vendavo.mesos.yowie.api.domain.Task
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.statistics.Statistics
import spock.lang.Specification

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Created by vtajzich
 */
class InMemoryStatisticsServiceSpec extends Specification {
    
    InMemoryStatisticsService service
    
    def setup() {
        service = new InMemoryStatisticsService()
    }

    def "should return statistics for task"() {
       
        given:
        
        LocalDateTime may21st10oClock = LocalDateTime.of(2015, 5, 21, 10, 0, 0, 0)
        
        TaskContext context1 = new TaskContext(new Task(name: 'task-1'))
        context1.setStartTime(may21st10oClock)
        context1.setEndTime(may21st10oClock.plusMinutes(11))
        
        when:
        
        service.storeTaskStatistics(context1)
     
        Optional<Statistics> statistics = service.getStatisticsForTask(context1.task)
        
        then:
        
        statistics.present
        
        statistics.get().getAverageTime(ChronoUnit.MINUTES).toMinutes() == 11
        statistics.get().getPercentil95Time(ChronoUnit.MINUTES).toMinutes() == 11
    }

    def "should aggregate statistics for same task"() {

        given:

        LocalDateTime may21st10oClock = LocalDateTime.of(2015, 5, 21, 10, 0, 0, 0)
        LocalDateTime may22nd13pmOClock = LocalDateTime.of(2015, 5, 22, 13, 0, 0, 0)

        TaskContext context1 = new TaskContext(new Task(name: 'task-1'))
        context1.setStartTime(may21st10oClock)
        context1.setEndTime(may21st10oClock.plusMinutes(11))

        TaskContext context2 = new TaskContext(new Task(name: 'task-1'))
        context2.setStartTime(may22nd13pmOClock)
        context2.setEndTime(may22nd13pmOClock.plusMinutes(20))

        when:

        service.storeTaskStatistics(context1)
        service.storeTaskStatistics(context2)

        Optional<Statistics> statistics = service.getStatisticsForTask(context1.task)

        then:

        statistics.present

        statistics.get().getAverageTime(ChronoUnit.MINUTES).toMillis() == 930_000
        statistics.get().getPercentil95Time(ChronoUnit.MINUTES).toMillis() == 1200_000
    }

    def "should return statistics for group"() {

        given:

        LocalDateTime may21st10oClock = LocalDateTime.of(2015, 5, 21, 10, 0, 0, 0)

        Task task1 = new Task(name: 'task-1')
        
        GroupContext groupContext = new GroupContext(new Group().addTask(task1))

        TaskContext context1 = groupContext.getTaskContext(task1.id)
        
        context1.setStartTime(may21st10oClock)
        context1.setEndTime(may21st10oClock.plusMinutes(11))

        when:

        service.storeGroupStatistics(groupContext)

        Optional<Statistics> statistics = service.getStatisticsForGroup(groupContext.group)

        then:

        statistics.present

        statistics.get().getAverageTime(ChronoUnit.MINUTES).toMinutes() == 11
        statistics.get().getPercentil95Time(ChronoUnit.MINUTES).toMinutes() == 11
    }

    def "should return aggregated statistics for same group"() {

        given:

        LocalDateTime may21st10oClock = LocalDateTime.of(2015, 5, 21, 10, 0, 0, 0)
        LocalDateTime may22nd13pmOClock = LocalDateTime.of(2015, 5, 22, 13, 0, 0, 0)

        Task task1 = new Task(name: 'task-1')
        GroupContext groupContext1 = new GroupContext(new Group(name: 'group-1').addTask(task1))
        TaskContext context1 = groupContext1.getTaskContext(task1.id)

        context1.setStartTime(may21st10oClock)
        context1.setEndTime(may21st10oClock.plusMinutes(11))

        
        Task task2 = new Task(name: 'task-1')
        GroupContext groupContext2 = new GroupContext(new Group(name: 'group-1').addTask(task2))
        TaskContext context2 = groupContext2.getTaskContext(task2.id)

        context2.setStartTime(may22nd13pmOClock)
        context2.setEndTime(may22nd13pmOClock.plusMinutes(20))

        when:

        service.storeGroupStatistics(groupContext1)
        service.storeGroupStatistics(groupContext2)

        Optional<Statistics> statistics = service.getStatisticsForGroup(groupContext1.group)

        then:

        statistics.present

        statistics.get().getAverageTime(ChronoUnit.MINUTES).toMillis() == 930_000
        statistics.get().getPercentil95Time(ChronoUnit.MINUTES).toMillis() == 1200_000
    }

    def "should return aggregated statistics for same group w/ mixed start and end times"() {

        given:

        LocalDateTime may21st10oClock = LocalDateTime.of(2015, 5, 21, 10, 0, 0, 0)
        LocalDateTime between = LocalDateTime.of(2015, 5, 21, 11, 0, 0, 0)
        LocalDateTime may22nd13pmOClock = LocalDateTime.of(2015, 5, 22, 13, 0, 0, 0)

        Task task1 = new Task(name: 'task-1')
        Task task2 = new Task(name: 'task-2')
        Task task3 = new Task(name: 'task-3')
        
        GroupContext groupContext1 = new GroupContext(new Group(name: 'group-1').addTask(task1).addTask(task2).addTask(task3))
        
        TaskContext context1 = groupContext1.getTaskContext(task1.id)
        context1.setStartTime(may21st10oClock)
        context1.setEndTime(may21st10oClock.plusMinutes(11))

        TaskContext context2 = groupContext1.getTaskContext(task2.id)
        context2.setStartTime(may22nd13pmOClock)
        context2.setEndTime(may22nd13pmOClock.plusMinutes(11))

        TaskContext context3 = groupContext1.getTaskContext(task3.id)
        context3.setStartTime(between)
        context3.setEndTime(between.plusMinutes(11))

        when:

        service.storeGroupStatistics(groupContext1)

        Optional<Statistics> statistics = service.getStatisticsForGroup(groupContext1.group)

        then:

        statistics.present

        statistics.get().getAverageTime(ChronoUnit.MINUTES).toMinutes() == 1631
        statistics.get().getPercentil95Time(ChronoUnit.MINUTES).toMinutes() == 1631
    }
}
