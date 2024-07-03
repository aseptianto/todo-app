package com.andrio.todoapp.service;

import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    @Autowired
    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getAllTodosByUserId(Long userId) {
        return todoRepository.findByUserId(userId);
    }
}