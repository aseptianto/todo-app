package com.andrio.todoapp.util;

import com.andrio.todoapp.dto.TodoUserDto;
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

    public String generateToken(Long userId, String email, String name) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put(Claims.SUBJECT, email);
            claims.put("userId", userId);
            claims.put("name", name);
            claims.put(Claims.ISSUED_AT, new Date(System.currentTimeMillis()));
            claims.put(Claims.EXPIRATION, new Date(System.currentTimeMillis() + EXPIRATION_TIME));

            return Jwts.builder()
                    .claims(claims)
                    .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public TodoUserDto extractUserDetails(String token) {
        Claims claims = extractAllClaims(token);
        return new TodoUserDto(claims.get("userId", Long.class), claims.getSubject(), claims.get("name", String.class));
    }
}