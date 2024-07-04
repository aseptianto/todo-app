package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.TodoCreateDto;
import com.andrio.todoapp.dto.TodoUpdateDto;
import com.andrio.todoapp.model.Role;
import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.model.TodoUserAssociation;
import com.andrio.todoapp.repository.TodoRepository;
import com.andrio.todoapp.repository.TodoUserAssociationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TodoServiceTests {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoUserAssociationRepository todoUserAssociationRepository;

    @InjectMocks
    private TodoService todoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFilteredTodosReturnsTodosForValidCriteria() {
        when(todoRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(Collections.singletonList(new Todo()));
        List<Todo> todos = todoService.getFilteredTodos(1L, Status.NOT_STARTED, LocalDate.now(), LocalDate.now().plusDays(1), "dueDate", "asc");
        assertThat(todos).isNotEmpty();
    }

    @Test
    void getFilteredTodosThrowsExceptionForInvalidSortField() {
        assertThatThrownBy(() -> todoService.getFilteredTodos(1L, Status.NOT_STARTED, LocalDate.now(), LocalDate.now().plusDays(1), "invalidField", "asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid sort field");
    }

    @Test
    void createTodoSuccessfullyCreatesTodo() {
        TodoCreateDto todoCreateDto = new TodoCreateDto("Test Todo", "Description", LocalDate.now(), Status.NOT_STARTED, 1);
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> i.getArguments()[0]);
        when(todoUserAssociationRepository.save(any(TodoUserAssociation.class))).thenAnswer(i -> i.getArguments()[0]);

        Todo result = todoService.createTodo(todoCreateDto, 1L);
        assertThat(result.getName()).isEqualTo(todoCreateDto.getName());
    }

    @Test
    void updateTodoSuccessfullyUpdatesExistingTodo() {
        TodoUpdateDto todoUpdateDto = new TodoUpdateDto("Updated Name", "Updated Description", LocalDate.now(), Status.COMPLETED, 2);
        Todo existingTodo = new Todo();
        existingTodo.setId(1L);
        TodoUserAssociation association = new TodoUserAssociation();
        association.setTodoUserId(1L);
        association.setTodoId(1L);
        existingTodo.setTodoUserAssociations(Collections.singletonList(association));

        when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(existingTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> i.getArguments()[0]);

        Todo result = todoService.updateTodo(1L, 1L, todoUpdateDto);
        assertThat(result.getName()).isEqualTo(todoUpdateDto.getName());
    }

    @Test
    void deleteTodoIfOwnerSuccessfullyDeletesTodo() {
        Todo existingTodo = new Todo();
        existingTodo.setId(1L);
        TodoUserAssociation association = new TodoUserAssociation();
        association.setRole(Role.OWNER);
        association.setTodoUserId(1L);
        association.setTodoId(1L);
        existingTodo.setTodoUserAssociations(Collections.singletonList(association));

        when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(existingTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> i.getArguments()[0]);

        boolean result = todoService.deleteTodoIfOwner(1L, 1L);
        assertThat(result).isTrue();
    }

    @Test
    void deleteTodoIfOwnerFailsForNonExistentTodo() {
        when(todoRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        assertThat(todoService.deleteTodoIfOwner(1L, 1L)).isFalse();
    }

    @Test
    void deleteTodoIfOwnerFailsForNonOwner() {
        Todo existingTodo = new Todo();
        existingTodo.setId(1L);
        TodoUserAssociation association = new TodoUserAssociation();
        association.setRole(Role.COLLABORATOR);
        association.setTodoUserId(1L);
        association.setTodoId(1L);
        existingTodo.setTodoUserAssociations(Collections.singletonList(association));

        when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(existingTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> i.getArguments()[0]);

        boolean result = todoService.deleteTodoIfOwner(1L, 1L);
        assertThat(result).isFalse();
    }

}
