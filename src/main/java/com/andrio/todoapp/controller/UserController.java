package com.andrio.todoapp.controller;

import com.andrio.todoapp.model.User;
import com.andrio.todoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    // This is a placeholder for the service that fetches the users.
    // You would typically use @Autowired to inject this service.
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAll() {
        return new ArrayList<User>();
//        return userService.getAllUsers();
    }
}
