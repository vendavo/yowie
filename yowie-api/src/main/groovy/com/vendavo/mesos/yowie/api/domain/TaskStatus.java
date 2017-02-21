package com.vendavo.mesos.yowie.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by vtajzich
 */
public interface TaskStatus {

    String getValue();

    boolean isFinal();

    boolean isTermination();

    @JsonCreator
    static TaskStatus createInstance(String newValue) {

        Optional<TaskStatus> staticStatus = Stream.of(StaticTaskStatus.values())
                .filter(status -> status.getValue().equals(newValue))
                .map((status) -> (TaskStatus) status)
                .findFirst();

        return staticStatus.orElse(new CustomTaskStatus(newValue));
    }
}
