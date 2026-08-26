package com.lin.monkey_finance.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

//spring creates one instance of this class and manages it
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder){this.jwtDecoder = jwtDecoder;}

    //"<?>" because the method is for headers and it's egal which payload type the message has
    // this method is called befor the msg is sent
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){

        //taking service part of the message (stomp headers (accessor))
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // if accessor != null it means the headers were received
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())){
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")){
                String token = authHeader.substring(7);

                try {
                    Jwt jwt = jwtDecoder.decode(token);

                    String userId = jwt.getSubject();

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.emptyList()
                    );

                    // binding authorized user to the current websocket session
                    accessor.setUser(auth);
                } catch (Exception e){
                    throw new IllegalArgumentException("Invalid jwt token in WebSocket connection", e);
                }
            }
        }
        return message;
    }
}
