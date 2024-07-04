package com.andrio.todoapp.controller;

import com.andrio.todoapp.dto.ErrorResponse;
import com.andrio.todoapp.dto.TodoCreateDto;
import com.andrio.todoapp.dto.TodoUpdateDto;
import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.time.LocalDate;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling todo requests.
 * Provides CRUD endpoints for todo items
 */
@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    /**
     * Constructs a TodoController with a TodoService.
     * @param todoService The todo service to be used for todo business logic.
     */
    @Autowired
    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    /**
     * Retrieves all todos for a user based on the provided filters.
     * @param status The status of the todos to filter by. Refer to Status definition
     * @param startDate The start date to filter by
     * @param endDate The end date to filter by
     * @param sort The field to sort the todo items by
     * @param sortDirection The direction to sort the todo items by asc or desc
     * @param request The HttpServletRequest object
     * @return A ResponseEntity containing the list of filtered and sorted todos
     */

    @Operation(summary = "Get all todos", description = "Retrieves all todos for a user based on the provided filters.",
            parameters = {
                    @Parameter(name = "status", description = "The status of the todos to filter by"),
                    @Parameter(name = "startDate", description = "The start date to filter by"),
                    @Parameter(name = "endDate", description = "The end date to filter by"),
                    @Parameter(name = "sort", description = "The field to sort the todo items by"),
                    @Parameter(name = "sortDirection", description = "The direction to sort the todo items by asc or desc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful retrieval of todos"),
                    @ApiResponse(responseCode = "400", description = "Bad request, invalid filter parameters"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(required = false) Status status,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             @RequestParam(required = false, defaultValue = "dueDate") String sort,
                             @RequestParam(required = false) String sortDirection,
                             HttpServletRequest request) {

        try {
            Long userId = ((TodoUserDto) request.getAttribute("todoUserDTO")).getId();
            return ResponseEntity.ok(todoService.getFilteredTodos(userId, status, startDate, endDate, sort, sortDirection));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Whoops! Something went wrong. Please try again later."));
        }
    }

    /**
     * Creates a new todo item for the user.
     *
     * @param todoCreateDto The DTO containing the new todo item details
     * @param request The HttpServletRequest object
     * @return A ResponseEntity containing the created todo item
     */
    @Operation(summary = "Create a new todo", description = "Creates a new todo item for the user.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TodoCreateDto.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Todo created"),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
            })
    @PostMapping
    public ResponseEntity<Todo> createTodo(@Valid @RequestBody TodoCreateDto todoCreateDto, HttpServletRequest request) {
        Long userId = ((TodoUserDto) request.getAttribute("todoUserDTO")).getId();
        Todo createdTodo = todoService.createTodo(todoCreateDto, userId);
        return new ResponseEntity<>(createdTodo, HttpStatus.CREATED);
    }

    /**
     * Updates an existing todo item for the user.
     * @param todoId The ID of the todo item to update
     * @param todoUpdateDto The DTO containing the updated todo item details
     * @param request The HttpServletRequest object
     * @return A ResponseEntity containing the updated todo item
     */
    @Operation(summary = "Update a todo", description = "Updates an existing todo item for the user.",
            parameters = {
                    @Parameter(name = "todoId", description = "The ID of the todo item to update")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = TodoUpdateDto.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Todo updated"),
                    @ApiResponse(responseCode = "400", description = "Bad request, invalid update parameters"),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
            })
    @PutMapping("/{todoId}")
    public ResponseEntity<?> updateTodo(@PathVariable Long todoId, @Valid @RequestBody TodoUpdateDto todoUpdateDto, HttpServletRequest request) {
        Long userId = ((TodoUserDto) request.getAttribute("todoUserDTO")).getId();

        try {
            Todo updatedTodo = todoService.updateTodo(todoId, userId, todoUpdateDto);
            return new ResponseEntity<>(updatedTodo, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Whoops! Something went wrong. Please try again later."));
        }
    }

    /**
     * Handles validation exceptions for the todo item DTOs.
     * @param ex The MethodArgumentNotValidException thrown during validation
     * @return A ResponseEntity containing the validation errors
     */
    @Operation(summary = "Handle validation exceptions", description = "Handles validation exceptions for the todo item DTOs.",
            responses = {
                    @ApiResponse(responseCode = "400", description = "Bad request, invalid todo item parameters")
            })
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Deletes a todo item for the user. The user must be the owner of the todo item.
     * @param todoId The ID of the todo item to delete
     * @param request The HttpServletRequest object
     * @return A ResponseEntity indicating the outcome of the delete operation
     */
    @Operation(summary = "Delete a todo", description = "Deletes a todo item for the user. The user must be the owner of the todo item.",
            parameters = {
                    @Parameter(name = "todoId", description = "The ID of the todo item to delete")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Todo deleted"),
                    @ApiResponse(responseCode = "400", description = "Bad request, user is not the owner of the todo or todo does not exist"),
                    @ApiResponse(responseCode = "500", description = "Internal server error",
                            content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = ErrorResponse.class)))
            })
    @DeleteMapping("/{todoId}")
    public ResponseEntity<?> deleteTodo(@PathVariable Long todoId, HttpServletRequest request) {
        Long userId = ((TodoUserDto) request.getAttribute("todoUserDTO")).getId();
        try {
            boolean isDeleted = todoService.deleteTodoIfOwner(todoId, userId);
            if (isDeleted) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("User is not the owner of the todo or todo does not exist"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Whoops! Something went wrong. Please try again later."));
        }
    }
}
