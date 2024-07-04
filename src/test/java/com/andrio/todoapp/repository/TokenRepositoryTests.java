package com.andrio.todoapp.repository;

import com.andrio.todoapp.repository.TokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class TokenRepositoryTests {

    private TokenRepository tokenRepository;

    @BeforeEach
    void setUp() {
        tokenRepository = new TokenRepository();
    }

    @Test
    void addingInvalidTokenMakesItUnavailable() {
        String token = "invalidToken123";
        tokenRepository.addInvalidToken(token);

        assertThat(tokenRepository.isTokenInvalid(token)).isTrue();
    }

    @Test
    void checkingNonExistentTokenReturnsFalse() {
        String token = "nonExistentToken123";

        assertThat(tokenRepository.isTokenInvalid(token)).isFalse();
    }

    @Test
    void addingMultipleInvalidTokensRetainsAll() {
        String token1 = "invalidToken1";
        String token2 = "invalidToken2";
        tokenRepository.addInvalidToken(token1);
        tokenRepository.addInvalidToken(token2);

        assertThat(tokenRepository.isTokenInvalid(token1)).isTrue();
        assertThat(tokenRepository.isTokenInvalid(token2)).isTrue();
    }

    @Test
    void addingSameTokenTwiceDoesNotCauseIssues() {
        String token = "duplicateToken";
        tokenRepository.addInvalidToken(token);
        tokenRepository.addInvalidToken(token);

        assertThat(tokenRepository.isTokenInvalid(token)).isTrue();
    }
}