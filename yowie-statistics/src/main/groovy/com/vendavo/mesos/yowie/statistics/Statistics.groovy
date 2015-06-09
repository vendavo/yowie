package com.vendavo.mesos.yowie.statistics

import groovy.transform.CompileStatic
import groovy.transform.ToString

import java.time.Duration
import java.time.temporal.ChronoUnit

/**
 * Created by vtajzich
 */
@ToString
@CompileStatic
class Statistics {
    
    private final long averageTime
    private final long percentil95Time
    private ChronoUnit unit

    Statistics(double averageTime, double percentil95Time, ChronoUnit unit) {
        this.averageTime = averageTime as long
        this.percentil95Time = percentil95Time as long
        this.unit = unit
    }

    Duration getAverageTime(ChronoUnit unit) {
        return Duration.of(0, unit).plusMillis(averageTime)
    }
    
    Duration getPercentil95Time(ChronoUnit unit) {
        return Duration.of(0, unit).plusMillis(percentil95Time)
    }
}
