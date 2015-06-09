package com.vendavo.mesos.yowie.api.websocket

import com.vendavo.mesos.yowie.api.domain.GroupContext
import com.vendavo.mesos.yowie.api.domain.TaskContext
import com.vendavo.mesos.yowie.mesos.ResourcesAvailable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

/**
 * Created by vtajzich
 */
@Service
class WebSocketGateway {

    @Autowired
    SimpMessagingTemplate template
    
    void sendStatus(ResourcesAvailable resourcesAvailable) {
        template.convertAndSend('/topic/status/resources', resourcesAvailable)    
    }
    
    void sendGroupStatus(GroupContext context) {
        template.convertAndSend('/topic/groups', context)
    }

    void sendTaskStatus(TaskContext context) {
        template.convertAndSend('/topic/tasks', context)
    }
}
