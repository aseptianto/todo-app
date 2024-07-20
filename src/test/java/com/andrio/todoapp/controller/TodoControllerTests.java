package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.ErrorResponse;
import com.andrio.todoapp.dto.TodoCreateDto;
import com.andrio.todoapp.dto.TodoUpdateDto;
import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.model.TodoUserAssociation;
import com.andrio.todoapp.service.TodoService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TodoControllerTests {

    @Mock
    private TodoService todoService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TodoController todoController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllTodosReturnsTodosForValidRequest() {
        when(request.getAttribute("todoUserDTO")).thenReturn(new TodoUserDto(1L, "", ""));
        when(todoService.getFilteredTodos(anyLong(), any(), any(), any(), any(), any())).thenReturn(List.of(new Todo()));

        ResponseEntity<?> response = todoController.getAll(null, null, null, "dueDate", null, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        boolean notEmpty = !((List<?>) response.getBody()).isEmpty();
        assertTrue(notEmpty);
    }

    @Test
    void getAllTodosReturnsBadRequestForInvalidParameters() {
        when(request.getAttribute("todoUserDTO")).thenReturn(new TodoUserDto(1L, "", ""));
        when(todoService.getFilteredTodos(anyLong(), any(), any(), any(), any(), any())).thenThrow(new IllegalArgumentException("Invalid sort field"));

        ResponseEntity<?> response = todoController.getAll(Status.NOT_STARTED, LocalDate.now(), LocalDate.now().minusDays(1), "wrongField", "desc", request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createTodoReturnsCreatedTodoForValidRequest() {
        TodoCreateDto todoCreateDto = new TodoCreateDto("task1", "description1", LocalDate.now(), Status.NOT_STARTED, 1);
        when(request.getAttribute("todoUserDTO")).thenReturn(new TodoUserDto(1L, "", ""));
        when(todoService.createTodo(any(TodoCreateDto.class), anyLong())).thenReturn(new Todo());

        ResponseEntity<Todo> response = todoController.createTodo(todoCreateDto, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateTodoReturnsUpdatedTodoForValidRequest() {
        TodoUpdateDto todoUpdateDto = new TodoUpdateDto("task1", "description1", LocalDate.now(), Status.NOT_STARTED, 1, new ArrayList<>());
        when(request.getAttribute("todoUserDTO")).thenReturn(new TodoUserDto(1L, "", ""));
        when(todoService.updateTodo(anyLong(), anyLong(), any(TodoUpdateDto.class))).thenReturn(new Todo());

        ResponseEntity<?> response = todoController.updateTodo(1L, todoUpdateDto, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

}