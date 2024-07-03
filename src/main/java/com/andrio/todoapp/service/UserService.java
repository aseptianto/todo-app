package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.UserRegistrationDto;
import com.andrio.todoapp.exception.UserAlreadyExistsException;
import com.andrio.todoapp.exception.UserNotFoundException;
import com.andrio.todoapp.model.TodoUser;
import com.andrio.todoapp.repository.UserRepository;
import com.andrio.todoapp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    public List<TodoUser> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean validateUser(String email, String password) {
        TodoUser user = userRepository.findByEmail(email);
        if (null != user) {
            return passwordEncoder.matches(password, user.getPassword());
        }
        return false;
    }

    public Optional<String> loginUser(String email, String password) {
        TodoUser user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User with email " + email + " not found");
        }
        logger.error("User found: {}", user);
        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        if (passwordMatch) {
            return Optional.of(jwtUtil.generateToken(user.getId(), user.getEmail(), user.getName()));
        } else {
            return Optional.empty();
        }
    }

    public void logoutUser(String token) {
        tokenService.invalidateToken(token);
    }

    public TodoUser registerUser(UserRegistrationDto userRegistrationDto) {
        TodoUser existingUser = userRepository.findByEmail(userRegistrationDto.getEmail());
        if (existingUser != null) {
            throw new UserAlreadyExistsException("User with email " + userRegistrationDto.getEmail() + " already exists");
        }

        TodoUser user = new TodoUser();
        user.setEmail(userRegistrationDto.getEmail());
        user.setName(userRegistrationDto.getName());
        user.setPassword(passwordEncoder.encode(userRegistrationDto.getPassword()));
        return userRepository.save(user);
    }
}