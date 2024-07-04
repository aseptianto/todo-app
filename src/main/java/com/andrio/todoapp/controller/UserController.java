package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.ErrorResponse;
import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.dto.UserInfoDto;
import com.andrio.todoapp.dto.UserRegistrationDto;
import com.andrio.todoapp.exception.UserAlreadyExistsException;
import com.andrio.todoapp.model.TodoUser;
import com.andrio.todoapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * The controller for handling user requests.
 * Provides endpoints for user registration and retrieval.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Constructs a UserController with a UserService.
     * @param userService The user service to be used for user operations.
     */
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a user. given a UserRegistrationDto, this method will create a new user.
     * @param userRegistrationDto The user registration dto containing the user's information. Including email and password.
     * @return A ResponseEntity containing the newly created user or an error response if the user already exists.
     */
    @Operation(summary = "Registers a new user", description = "Registers a new user with the provided information.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = UserRegistrationDto.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created"),
                    @ApiResponse(responseCode = "409", description = "User already exists",
                            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto userRegistrationDto) {
        try {
            TodoUser newUser = userService.registerUser(userRegistrationDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Whoops! Something went wrong. Please try again later."));
        }
    }

    /**
     * Get the currently logged-in user's information.
     * @param request The HttpServletRequest object
     * @return A ResponseEntity containing the currently logged-in user's information.
     */
    @Operation(summary = "Get logged-in user info", description = "Retrieves the currently logged-in user's information.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of user info"),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
            })
    @GetMapping("/self")
    public ResponseEntity<?> getLoggedInUserInfo(HttpServletRequest request) {
        TodoUserDto user = ((TodoUserDto) request.getAttribute("todoUserDTO"));
        if (user != null) {
            UserInfoDto userInfo = UserInfoDto.builder()
                    .email(user.getEmail())
                    .name(user.getName())
                    .build();
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Whoops! Something went wrong. Please try again later."));
        }
    }
}
