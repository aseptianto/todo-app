package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.TodoUserDTO;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.service.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {

    // This is a placeholder for the service that fetches the users.
    // You would typically use @Autowired to inject this service.
    private final TodoService todoService;

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    @Autowired
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public List<Todo> getAll(@RequestParam(required = false) String filter, HttpServletRequest request) {
        Long userId = ((TodoUserDTO) request.getAttribute("todoUserDTO")).getId();
        return todoService.getAllTodosByUserId(userId);
    }
}
