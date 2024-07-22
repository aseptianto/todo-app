package com.andrio.todoapp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class WebSocketUserRegistry {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private final static String WEBSOCKET_USER_REGISTRY = "WEBSOCKET_USER_REGISTRY:";

    public void registerUserSession(Long userId, String sessionId) {
        redisTemplate.opsForValue().set(WEBSOCKET_USER_REGISTRY + userId.toString(), sessionId);
    }

    public void removeUserSession(Long userId) {
        redisTemplate.opsForValue().getOperations().delete(WEBSOCKET_USER_REGISTRY + userId.toString());
    }

    public String getSessionIdByUserId(Long userId) {
        return redisTemplate.opsForValue().get(WEBSOCKET_USER_REGISTRY + userId.toString());
    }
}
