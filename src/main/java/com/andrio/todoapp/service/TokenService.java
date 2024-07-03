package com.andrio.todoapp.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.andrio.todoapp.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void invalidateToken(String token) {
        token = token.replace("Bearer ", "");
        tokenRepository.addInvalidToken(token);
    }

    public boolean isTokenInvalid(String token) {
        return tokenRepository.isTokenInvalid(token);
    }
}