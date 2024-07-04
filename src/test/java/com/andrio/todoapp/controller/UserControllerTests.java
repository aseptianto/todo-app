package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.ErrorResponse;
import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.dto.UserInfoDto;
import com.andrio.todoapp.dto.UserRegistrationDto;
import com.andrio.todoapp.exception.UserAlreadyExistsException;
import com.andrio.todoapp.model.TodoUser;
import com.andrio.todoapp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTests {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUserReturnsCreatedStatusAndUserDetails() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("","","");
        TodoUser expectedUser = new TodoUser();
        when(userService.registerUser(registrationDto)).thenReturn(expectedUser);

        ResponseEntity<?> response = userController.registerUser(registrationDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expectedUser, response.getBody());
    }

    @Test
    void registerUserReturnsConflictWhenUserAlreadyExists() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("", "", "");
        when(userService.registerUser(registrationDto)).thenThrow(new UserAlreadyExistsException("User already exists"));

        ResponseEntity<?> response = userController.registerUser(registrationDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already exists", ((ErrorResponse) response.getBody()).getMsg());
    }

    @Test
    void registerUserReturnsInternalServerErrorOnUnexpectedException() {
        UserRegistrationDto registrationDto = new UserRegistrationDto("", "", "");
        when(userService.registerUser(registrationDto)).thenThrow(RuntimeException.class);

        ResponseEntity<?> response = userController.registerUser(registrationDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Whoops! Something went wrong. Please try again later.", ((ErrorResponse) response.getBody()).getMsg());
    }

    @Test
    void getLoggedInUserInfoReturnsUserInfo() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TodoUserDto mockUserDto = new TodoUserDto(1L, "andrio", "andrio@email.com");
        when(mockRequest.getAttribute("todoUserDTO")).thenReturn(mockUserDto);

        ResponseEntity<?> response = userController.getLoggedInUserInfo(mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(UserInfoDto.class, response.getBody());

        UserInfoDto userInfo = (UserInfoDto) response.getBody();
        assertEquals("andrio@email.com", userInfo.getEmail());
        assertEquals("andrio", userInfo.getName());
    }

    @Test
    void getLoggedInUserInfoReturnsError() {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        TodoUserDto mockUserDto = new TodoUserDto(1L, "andrio", "andrio@email.com");
        when(mockRequest.getAttribute("todoUserDTO")).thenReturn(null);

        ResponseEntity<?> response = userController.getLoggedInUserInfo(mockRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertInstanceOf(ErrorResponse.class, response.getBody());

        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Whoops! Something went wrong. Please try again later.", errorResponse.getMsg());

    }
}