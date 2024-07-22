package com.andrio.todoapp.config;

import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.middleware.AuthHandshakeInterceptor;
import com.andrio.todoapp.repository.WebSocketUserRegistry;
import com.andrio.todoapp.service.TokenService;
import com.andrio.todoapp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.server.HandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final TokenService tokenService;
    private final JwtUtil jwtUtil;
    private final WebSocketUserRegistry webSocketUserRegistry;

    private final static Logger logger = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    public WebSocketConfig(TokenService tokenService, JwtUtil jwtUtil, WebSocketUserRegistry webSocketUserRegistry) {
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.webSocketUserRegistry = webSocketUserRegistry;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").addInterceptors(authenticationInterceptor());
    }

    @Bean
    public HandshakeInterceptor authenticationInterceptor() {
        return new AuthHandshakeInterceptor(tokenService, jwtUtil);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                logger.info("Adding websocket session {}", session.getId());
                TodoUserDto todoUserDto = (TodoUserDto) session.getAttributes().get("todoUserDTO");
                if (todoUserDto != null) {
                    webSocketUserRegistry.registerUserSession(todoUserDto.getId(), session.getId());
                }
                super.afterConnectionEstablished(session);
            }

            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                webSocketUserRegistry.removeUserSession(session.getId());
                super.afterConnectionClosed(session, closeStatus);
            }
        });
    }
}
