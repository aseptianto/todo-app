package com.andrio.todoapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    public String generateToken(String email) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put(Claims.SUBJECT, email);
            claims.put(Claims.ISSUED_AT, new Date(System.currentTimeMillis()));
            claims.put(Claims.EXPIRATION, new Date(System.currentTimeMillis() + EXPIRATION_TIME));

            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }
}