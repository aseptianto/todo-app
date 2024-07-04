package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.TodoCreateDto;
import com.andrio.todoapp.dto.TodoUpdateDto;
import com.andrio.todoapp.model.Role;
import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.model.TodoUserAssociation;
import com.andrio.todoapp.repository.TodoRepository;
import com.andrio.todoapp.repository.TodoUserAssociationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoUserAssociationRepository todoUserAssociationRepository;
    private final Set<String> allowedSortFields = Set.of("dueDate", "status", "name", "priority");

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public TodoService(TodoRepository todoRepository, TodoUserAssociationRepository todoUserAssociationRepository) {
        this.todoRepository = todoRepository;
        this.todoUserAssociationRepository = todoUserAssociationRepository;
    }

    public List<Todo> getFilteredTodos(Long userId, Status status, LocalDate startDate, LocalDate endDate, String sort, String sortDirection) {
        if (!allowedSortFields.contains(sort)) {
            throw new IllegalArgumentException("Invalid sort field");
        }
        Specification<Todo> spec = TodoRepository.hasStatusAndDueDateBetween(userId, status, startDate, endDate);
        Sort sorter = "desc".equalsIgnoreCase(sortDirection) ? Sort.by(sort).descending() : Sort.by(sort).ascending();
        return todoRepository.findAll(spec, sorter);
    }

    @Transactional
    public Todo createTodo(TodoCreateDto todoCreateDto, Long userId) {
        UUID uuid = UUID.randomUUID();
        Todo todo = Todo.builder()
                .name(todoCreateDto.getName())
                .uuid(uuid.toString())
                .isDeleted(false)
                .description(todoCreateDto.getDescription())
                .dueDate(todoCreateDto.getDueDate())
                .status(todoCreateDto.getStatus())
                .priority(todoCreateDto.getPriority().toString())
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        todo = todoRepository.save(todo);

        TodoUserAssociation association = TodoUserAssociation.builder()
                .todoId(todo.getId())
                .todoUserId(userId)
                .role(Role.OWNER)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
        todoUserAssociationRepository.save(association);

        ArrayList<TodoUserAssociation> associations = new ArrayList<>();
        associations.add(association);
        todo.setTodoUserAssociations(associations);
        return todo;
    }

    @Transactional
    public Todo updateTodo(Long todoId, Long userId, TodoUpdateDto todoUpdateDto) {
        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new EntityNotFoundException("Todo not found"));

        TodoUserAssociation association = todo.getTodoUserAssociations().stream()
                .filter(a -> a.getTodoUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not associated with todo"));

        todo.setName(todoUpdateDto.getName());
        todo.setDescription(todoUpdateDto.getDescription());
        todo.setDueDate(todoUpdateDto.getDueDate());
        todo.setStatus(todoUpdateDto.getStatus());
        todo.setPriority(todoUpdateDto.getPriority().toString());
        todo.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return todoRepository.save(todo);
    }

    @Transactional
    public boolean deleteTodoIfOwner(Long todoId, Long userId) {
        Todo todo = todoRepository.findById(todoId).orElse(null);
        if (todo == null) {
            return false;
        }

        TodoUserAssociation association = todo.getTodoUserAssociations().stream()
                .filter(a -> a.getTodoUserId().equals(userId))
                .findFirst()
                .orElse(null);
        if (association == null) {
            return false;
        }

        if (association.getRole() == Role.OWNER) {
            // user is owner, mark as deleted
            todo.setDeleted(true);
            todoRepository.save(todo);
            return true;
        }
        return false;
    }
}