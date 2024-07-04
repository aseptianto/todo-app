package com.andrio.todoapp.controller;

import com.andrio.todoapp.controller.LogoutController;
import com.andrio.todoapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogoutControllerTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private LogoutController logoutController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void logoutSuccessfullyReturnsNoContent() {
        String token = "Bearer validToken";

        ResponseEntity<?> response = logoutController.logout(token);

        verify(userService).logoutUser(token);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}