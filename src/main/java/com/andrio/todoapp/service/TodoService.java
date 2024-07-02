package com.andrio.todoapp.service;

import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.model.TodoUser;
import com.andrio.todoapp.repository.TodoRepository;
import com.andrio.todoapp.repository.UserRepository;
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

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }
}