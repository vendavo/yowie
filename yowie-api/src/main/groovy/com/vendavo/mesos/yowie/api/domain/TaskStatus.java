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
                .filter((StaticTaskStatus s) -> s.getValue().equals(newValue))
                .map((s) -> (TaskStatus) s)
                .findFirst();

        return staticStatus.orElse(new CustomTaskStatus(newValue));
    }
}
