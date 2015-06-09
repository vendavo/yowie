package com.vendavo.mesos.yowie.api.websocket

import groovy.transform.CompileStatic
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

/**
 * Created by vtajzich
 */
@Configuration
@EnableWebSocketMessageBroker
@CompileStatic
class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic")
        config.setApplicationDestinationPrefixes("/app")
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/status").setAllowedOrigins('*').withSockJS()
        registry.addEndpoint("/ws/tasks").setAllowedOrigins('*').withSockJS()
        registry.addEndpoint("/ws/groups").setAllowedOrigins('*').withSockJS()
    }
}
