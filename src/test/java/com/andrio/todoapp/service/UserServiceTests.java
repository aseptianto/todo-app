package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.UserRegistrationDto;
import com.andrio.todoapp.exception.UserAlreadyExistsException;
import com.andrio.todoapp.exception.UserNotFoundException;
import com.andrio.todoapp.model.TodoUser;
import com.andrio.todoapp.repository.UserRepository;
import com.andrio.todoapp.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsersReturnsListOfUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(new TodoUser(), new TodoUser()));
        assertThat(userService.getAllUsers()).hasSize(2);
    }

    @Test
    void validateUserReturnsTrueForValidCredentials() {
        TodoUser user = new TodoUser();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        assertThat(userService.validateUser("test@example.com", "password")).isTrue();
    }

    @Test
    void validateUserReturnsFalseForInvalidCredentials() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        assertThat(userService.validateUser("test@example.com", "password")).isFalse();
    }

    @Test
    void loginUserReturnsTokenForValidCredentials() {
        TodoUser user = new TodoUser();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(user.getId(), user.getEmail(), user.getName())).thenReturn("token");
        assertThat(userService.loginUser("test@example.com", "password")).isEqualTo(Optional.of("token"));
    }

    @Test
    void loginUserThrowsExceptionForNonExistentUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(null);
        assertThatThrownBy(() -> userService.loginUser("test@example.com", "password"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with email test@example.com not found");
    }

    @Test
    void loginUserThrowsExceptionForExistingUserButWrongPassword() {
        TodoUser user = new TodoUser();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(false);
        assertThat(userService.loginUser("test@example.com", "password")).isEqualTo(Optional.empty());

    }

    @Test
    void registerUserThrowsExceptionForExistingEmail() {
        UserRegistrationDto dto = new UserRegistrationDto("test@example.com", "Test User", "password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(new TodoUser());
        assertThatThrownBy(() -> userService.registerUser(dto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with email test@example.com already exists");
    }

    @Test
    void logoutUserCallsTokenInvalidation() {
        String token = "sampleToken";
        userService.logoutUser(token);
        verify(tokenService).invalidateToken(token);
    }

    @Test
    void registerUserSavesNewUserForNewEmail() {
        UserRegistrationDto newUser = new UserRegistrationDto("new@example.com", "New User", "password");
        TodoUser savedUser = new TodoUser();
        savedUser.setEmail(newUser.getEmail());
        savedUser.setName(newUser.getName());

        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(null);
        when(userRepository.save(any(TodoUser.class))).thenAnswer(invocation -> {
            TodoUser user = invocation.getArgument(0);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return user;
        });

        TodoUser result = userService.registerUser(newUser);

        assertThat(result.getEmail()).isEqualTo(newUser.getEmail());
        assertThat(result.getName()).isEqualTo(newUser.getName());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(TodoUser.class));
    }

}