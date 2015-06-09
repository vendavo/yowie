package com.vendavo.mesos.yowie.api.websocket

import com.vendavo.mesos.yowie.mesos.ResourcesAvailable
import com.vendavo.mesos.yowie.mesos.YowieFramework
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

/**
 * Created by vtajzich
 */
@Controller
@CompileStatic
class WSStatusController {
    
    @Autowired
    YowieFramework framework
    
    @MessageMapping('/status')
    @SendTo('/status/resources')
    ResourcesAvailable availableResources() {
        return framework.availableResources
    }
}
