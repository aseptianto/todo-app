package com.andrio.todoapp.controller;

import com.andrio.todoapp.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logout")
public class LogoutController {

    private final TokenService tokenService;

    @Autowired
    public LogoutController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping()
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        tokenService.invalidateToken(token);
        return ResponseEntity.noContent().build();
    }
}
