package com.andrio.todoapp.controller;

import com.andrio.todoapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling logout requests.
 * This controller provides an endpoint for users to log out of the application.
 */
@RestController
@RequestMapping("/logout")
public class LogoutController {

    private final UserService userService;

    /**
     * Constructs a LogoutController with a UserService.
     *
     * @param userService The user service to be used for logout operations.
     */
    @Autowired
    public LogoutController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes a logout request.
     * This method invalidates the user's token, effectively logging them out of the application.
     * Due to the nature of JWT, this logout method will add the token into a blacklist, preventing it from being used again.
     *
     * @param token The authorization token to be invalidated.
     * @return A ResponseEntity indicating the outcome of the logout operation.
     */
    @Operation(summary = "Logs out a user", description = "Processes a logout request and invalidates the user's token.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successful logout"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @PostMapping()
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        userService.logoutUser(token);
        return ResponseEntity.noContent().build();
    }
}