package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.ErrorResponse;
import com.andrio.todoapp.dto.LoginRequest;
import com.andrio.todoapp.dto.LoginResponse;
import com.andrio.todoapp.exception.UserNotFoundException;
import com.andrio.todoapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Handles login requests for the application.
 */
@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;

    /**
     * Constructs a LoginController with a UserService.
     *
     * @param userService The user service to be used for login operations.
     */
    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes a login request.
     *
     * @param loginRequest The login request containing the user's email and password.
     * @return A ResponseEntity containing either a successful login response with a token,
     *         an error response for invalid credentials, or an internal server error message.
     */
    @Operation(summary = "Logs in a user", description = "Processes a login request and returns a token if successful.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(schema = @Schema(implementation = LoginRequest.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized, invalid credentials",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Optional<String> token = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());

            if (token.isPresent()) {
                LoginResponse loginResponse = new LoginResponse(token.get());
                return ResponseEntity.ok(loginResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid email or password"));
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid email or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Whoops! Something went wrong. Please try again later."));
        }
    }
}