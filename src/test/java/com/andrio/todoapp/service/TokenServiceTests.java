package com.andrio.todoapp.service;

import com.andrio.todoapp.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TokenServiceTests {

    @Mock
    private TokenRepository tokenRepository;

    @InjectMocks
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void invalidateTokenShouldAddTokenToInvalidTokens() {
        String token = "Bearer validToken";
        tokenService.invalidateToken(token);
        verify(tokenRepository).addInvalidToken("validToken");
    }

    @Test
    void isTokenInvalidShouldReturnTrueForInvalidToken() {
        String token = "invalidToken";
        when(tokenRepository.isTokenInvalid(token)).thenReturn(true);
        boolean result = tokenService.isTokenInvalid(token);
        assertThat(result).isTrue();
    }

    @Test
    void isTokenInvalidShouldReturnFalseForValidToken() {
        String token = "validToken";
        when(tokenRepository.isTokenInvalid(token)).thenReturn(false);
        boolean result = tokenService.isTokenInvalid(token);
        assertThat(result).isFalse();
    }
}
