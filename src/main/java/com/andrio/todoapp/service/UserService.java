package com.andrio.todoapp.service;

import com.andrio.todoapp.model.TodoUser;
import com.andrio.todoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}