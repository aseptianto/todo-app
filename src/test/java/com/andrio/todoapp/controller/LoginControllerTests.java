package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.ErrorResponse;
import com.andrio.todoapp.dto.LoginRequest;
import com.andrio.todoapp.dto.LoginResponse;
import com.andrio.todoapp.exception.UserNotFoundException;
import com.andrio.todoapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class LoginControllerTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        LoginRequest loginRequest = new LoginRequest("andrio@example.com", "password");
        when(userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword())).thenReturn(Optional.of("token"));

        ResponseEntity<?> response = loginController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token", ((LoginResponse) response.getBody()).getToken());
    }

    @Test
    void loginReturnsUnauthorizedForInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("andrio@example.com", "wrongPassword");
        when(userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword())).thenReturn(Optional.empty());

        ResponseEntity<?> response = loginController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", ((ErrorResponse) response.getBody()).getMsg());
    }

    @Test
    void loginReturnsUnauthorizedForNonExistentUser() {
        LoginRequest loginRequest = new LoginRequest("notexist@example.com", "password");
        when(userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword())).thenThrow(new UserNotFoundException(""));

        ResponseEntity<?> response = loginController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", ((ErrorResponse) response.getBody()).getMsg());
    }

    @Test
    void loginReturnsInternalServerErrorForUnexpectedException() {
        LoginRequest loginRequest = new LoginRequest("user@example.com", "password");
        when(userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword())).thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = loginController.login(loginRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Whoops! Something went wrong. Please try again later.", ((ErrorResponse) response.getBody()).getMsg());
    }
}
