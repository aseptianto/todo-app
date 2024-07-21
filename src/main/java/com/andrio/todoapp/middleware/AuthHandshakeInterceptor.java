package com.andrio.todoapp.middleware;

import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.exception.AuthorizationFailedException;
import com.andrio.todoapp.service.TokenService;
import com.andrio.todoapp.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final TokenService tokenService; // Service to verify token and extract user ID
    private final JwtUtil jwtUtil; // Utility class to extract user details from token

    private static final Logger logger = LoggerFactory.getLogger(AuthHandshakeInterceptor.class);

    public AuthHandshakeInterceptor(TokenService tokenService, JwtUtil jwtUtil) {
        this.tokenService = tokenService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//        String token = request.getHeaders().getFirst("Authorization");
        String token = null;
        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            token = query.split("token=")[1].split("&")[0];
        }
        try {
            if (token == null || token.isEmpty()) {
                throw new AuthorizationFailedException("Authorization header is missing");
            }
            if (tokenService.isTokenInvalid(token)) {
                throw new AuthorizationFailedException("Invalid token");
            }
            TodoUserDto todoUserDTO = jwtUtil.extractUserDetails(token);
            attributes.put("todoUserDTO", todoUserDTO);
            return true;
        } catch (AuthorizationFailedException | ExpiredJwtException e) {
            logger.error("Unable to verify token. Error: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        } catch (Exception e) {
            logger.error("Whoops! Something went wrong! Error: {}", e.getMessage());
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // Nothing to do after handshake
    }

    private String convertObjectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
