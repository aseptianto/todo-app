package com.andrio.todoapp.service;

import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.time.LocalDate;
import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    @Autowired
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getFilteredTodos(Long userId, Status status, LocalDate startDate, LocalDate endDate) {
        Specification<Todo> spec = TodoRepository.hasStatusAndDueDateBetween(userId, status, startDate, endDate);
        return todoRepository.findAll(spec);
    }
}