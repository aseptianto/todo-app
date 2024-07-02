package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.LoginRequest;
import com.andrio.todoapp.dto.LoginResponse;
import com.andrio.todoapp.service.UserService;
import com.andrio.todoapp.util.JwtUtil;
import com.mysql.cj.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        boolean isValid = userService.validateUser(loginRequest.getEmail(), loginRequest.getPassword());

        if (isValid) {
            String token = jwtUtil.generateToken(loginRequest.getEmail());
            LoginResponse loginResponse = new LoginResponse(token);
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }
}