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

    /**
     * Constructs a WebSockerConfig
     * @param tokenService Service for token operations
     * @param jwtUtil Utility for JWT operations
     * @param webSocketUserRegistry Registry for tracking WebSocket user sessions
     */
    @Autowired
    public WebSocketConfig(TokenService tokenService, JwtUtil jwtUtil, WebSocketUserRegistry webSocketUserRegistry) {
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
        this.webSocketUserRegistry = webSocketUserRegistry;
    }

    /**
     * Registers the STOMP endpoints to establish WebSocket communication.\
     *
     * @param registry StompEndpointRegistry for configuring endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").addInterceptors(authenticationInterceptor());
    }

    /**
     * Creates a HandshakeInterceptor for authenticating WebSocket connections
     * @return HandshakeInterceptor
     */
    @Bean
    public HandshakeInterceptor authenticationInterceptor() {
        return new AuthHandshakeInterceptor(tokenService, jwtUtil);
    }

    /**
     * Configures the message broker for WebSocket communication. It has /topic and /queue brokers
     * @param registry MessageBrokerRegistry
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Configures the WebSocket transport
     * @param registration WebSocketTransportRegistration
     */
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(handler -> new WebSocketHandlerDecorator(handler) {
            /**
             * Registers a user session after a WebSocket connection is established
             * @param session the websocket session created
             * @throws Exception for errors
             */
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                logger.info("Adding websocket session {}", session.getId());
                TodoUserDto todoUserDto = (TodoUserDto) session.getAttributes().get("todoUserDTO");
                if (todoUserDto != null) {
                    webSocketUserRegistry.registerUserSession(todoUserDto.getId(), session.getId());
                }
                super.afterConnectionEstablished(session);
            }

            /**
             * Removes a user session after a WebSocket connection is closed
             * @param session of the websocket
             * @param closeStatus CloseStatus
             * @throws Exception for errors
             */
            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                TodoUserDto todoUserDto = (TodoUserDto) session.getAttributes().get("todoUserDTO");
                if (todoUserDto != null) {
                    webSocketUserRegistry.removeUserSession(todoUserDto.getId());
                }
                super.afterConnectionClosed(session, closeStatus);
            }
        });
    }
}
