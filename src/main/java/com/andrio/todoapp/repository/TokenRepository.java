package com.andrio.todoapp.repository;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

@Repository
public class TokenRepository {

    @Autowired
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public TokenRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addInvalidToken(String token) {
        redisTemplate.opsForValue().set(token, "invalid");
    }

    public boolean isTokenInvalid(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(token));
    }
}