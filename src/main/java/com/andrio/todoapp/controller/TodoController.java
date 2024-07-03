package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.service.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

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
    public List<Todo> getAll(@RequestParam(required = false) Status status,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             HttpServletRequest request) {
        Long userId = ((TodoUserDto) request.getAttribute("todoUserDTO")).getId();
        logger.error(status.toString());
        logger.error(startDate.toString());
        logger.error(endDate.toString());
        return todoService.getFilteredTodos(userId, status, startDate, endDate);
    }
}
