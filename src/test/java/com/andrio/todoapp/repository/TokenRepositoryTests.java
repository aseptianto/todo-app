package com.andrio.todoapp.repository;

import com.andrio.todoapp.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TokenRepositoryTests {

    private TokenRepository tokenRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        tokenRepository = new TokenRepository(redisTemplate);
    }

    @Test
    void addingInvalidTokenMakesItUnavailable() {
        String token = "invalidToken123";
        tokenRepository.addInvalidToken(token);

        verify(valueOperations).set(token, "invalid");
    }

    @Test
    void checkingNonExistentTokenReturnsFalse() {
        String token = "nonExistentToken123";

        when(valueOperations.get(token)).thenReturn(null);

        assertThat(tokenRepository.isTokenInvalid(token)).isFalse();
    }

    @Test
    void addingMultipleInvalidTokensRetainsAll() {
        String token1 = "invalidToken1";
        String token2 = "invalidToken2";
        tokenRepository.addInvalidToken(token1);
        tokenRepository.addInvalidToken(token2);

        verify(valueOperations).set(token1, "invalid");
        verify(valueOperations).set(token2, "invalid");

        when(redisTemplate.hasKey(token1)).thenReturn(Boolean.TRUE);
        when(redisTemplate.hasKey(token2)).thenReturn(Boolean.TRUE);


        assertThat(tokenRepository.isTokenInvalid(token1)).isTrue();
        assertThat(tokenRepository.isTokenInvalid(token2)).isTrue();
    }

    @Test
    void addingSameTokenTwiceDoesNotCauseIssues() {
        String token = "duplicateToken";
        tokenRepository.addInvalidToken(token);
        tokenRepository.addInvalidToken(token);

        verify(valueOperations, times(2)).set(token, "invalid");
        when(redisTemplate.hasKey(token)).thenReturn(Boolean.TRUE);


        assertThat(tokenRepository.isTokenInvalid(token)).isTrue();
    }
}