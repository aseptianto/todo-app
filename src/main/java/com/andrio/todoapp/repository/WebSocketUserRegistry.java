package com.andrio.todoapp.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class WebSocketUserRegistry {
    private final ConcurrentHashMap<Long, String> userIdToSessionIdMap = new ConcurrentHashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(WebSocketUserRegistry.class);

    public void registerUserSession(Long userId, String sessionId) {
        userIdToSessionIdMap.put(userId, sessionId);
        logger.info("userregistry after adding {} : {}", sessionId, userIdToSessionIdMap);
    }

    public void removeUserSession(String sessionId) {
        userIdToSessionIdMap.values().remove(sessionId);
    }

    public String getSessionIdByUserId(Long userId) {
        logger.info("userregistry: {}", userIdToSessionIdMap);
        return userIdToSessionIdMap.get(userId);
    }
}
