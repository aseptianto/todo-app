package com.andrio.todoapp.repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRepository {

    private final Set<String> invalidTokens = Collections.synchronizedSet(new HashSet<>());

    public void addInvalidToken(String token) {
        invalidTokens.add(token);
    }

    public boolean isTokenInvalid(String token) {
        return invalidTokens.contains(token);
    }
}