package com.vendavo.mesos.yowie.api.rest

import com.vendavo.mesos.yowie.mesos.ResourcesAvailable
import com.vendavo.mesos.yowie.mesos.YowieFramework
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created by vtajzich
 */
@CompileStatic
@RequestMapping('/status')
@RestController
class StatusController {
    
    @Autowired
    YowieFramework framework
    
    @RequestMapping('/resources')
    ResourcesAvailable getResources() {
        return framework.getAvailableResources()
    }
}
