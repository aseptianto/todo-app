package com.andrio.todoapp.controller;

import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.model.TodoUser;
import com.andrio.todoapp.service.TodoService;
import com.andrio.todoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {

    // This is a placeholder for the service that fetches the users.
    // You would typically use @Autowired to inject this service.
    private final TodoService todoService;

    @Autowired
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<Todo> getAll() {
        return todoService.getAllTodos();
    }
}
