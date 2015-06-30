package com.vendavo.mesos.yowie.api.rest

import com.vendavo.mesos.yowie.api.domain.Group
import com.vendavo.mesos.yowie.api.domain.GroupContext
import com.vendavo.mesos.yowie.mesos.YowieFramework
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

import javax.validation.Valid

import static java.util.stream.Collectors.toList

/**
 * Created by vtajzich
 */
@CompileStatic
@RequestMapping('/groups')
@RestController
class GroupController {

    @Autowired
    YowieFramework framework

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = '', method = RequestMethod.POST)
    GroupContext create(@Valid @RequestBody Group group) {

        return framework.createTask(group)
    }

    @RequestMapping('')
    Collection<GroupContext> getAllGroups(Pageable pageable) {

        return framework.getAllGroupContexts()
                .skip(pageable.pageNumber * pageable.pageSize)
                .limit(pageable.pageSize)
                .collect(toList())
    }

    @RequestMapping('/status/finished')
    Collection<GroupContext> getFinishedGroups(Pageable pageable) {
        return framework.getFinishedGroupContexts()
                .sorted({ GroupContext lh, GroupContext rh -> -lh.startTime.compareTo(rh.startTime) } as Comparator<GroupContext>)
                .skip(pageable.pageNumber * pageable.pageSize)
                .limit(pageable.pageSize)
                .collect(toList())
    }
}
