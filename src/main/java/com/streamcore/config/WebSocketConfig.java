package com.streamcore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures WebSocket and STOMP messaging.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.thread-pool.core-size:10}")
    private int corePoolSize;

    @Value("${app.thread-pool.max-size:100}")
    private int maxPoolSize;

    @Value("${app.thread-pool.queue-capacity:500}")
    private int queueCapacity;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.taskExecutor().corePoolSize(corePoolSize).maxPoolSize(maxPoolSize).queueCapacity(queueCapacity);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registering the STOMP endpoint
        registry.addEndpoint("/stream-ws").setAllowedOriginPatterns("*").withSockJS();
        registry.addEndpoint("/stream-ws").setAllowedOriginPatterns("*"); // Also allow raw websocket
    }
}
