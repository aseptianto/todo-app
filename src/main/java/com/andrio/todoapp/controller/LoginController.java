package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.LoginRequest;
import com.andrio.todoapp.dto.LoginResponse;
import com.andrio.todoapp.service.UserService;
import com.andrio.todoapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public LoginController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<String> token = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

        if (token.isPresent()) {
            LoginResponse loginResponse = new LoginResponse(token.get());
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
}