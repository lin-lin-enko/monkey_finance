package com.lin.monkey_finance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
// enables websocket message handling
@EnableWebSocketMessageBroker
// the interface enables setting websocket parameters
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor interceptor;

    public WebSocketConfig(WebSocketAuthInterceptor interceptor){
        this.interceptor = interceptor;
    }

    //registering the interceptor for client inbound channel (from client browser's to backend)
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(interceptor);
    }

    // configuring message broker (address routing)
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        // "/topic" for mass sending
        // "/queue" for private messages
        registry.enableSimpleBroker("/topic", "/queue");

        // if browser will send a message to an address starting with "/app"
        // spring will pass it to the controllers with @MessageMapping annotation
        registry.setApplicationDestinationPrefixes("/app");

        // turns "/user/queue" into a specific session's address
        registry.setUserDestinationPrefix("/user");
    }

    // browser will connect by the following address: ws://localhost:8080/ws
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        //allows connections from any frontend-domain
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
}
