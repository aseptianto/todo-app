package com.andrio.todoapp.controller;

import com.andrio.todoapp.service.TokenService;
import com.andrio.todoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logout")
public class LogoutController {

    private final UserService userService;

    @Autowired
    public LogoutController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        userService.logoutUser(token);
        return ResponseEntity.noContent().build();
    }
}
