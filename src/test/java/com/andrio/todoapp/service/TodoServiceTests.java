package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.TodoCreateDto;
import com.andrio.todoapp.dto.TodoUpdateDto;
import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.model.Role;
import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import com.andrio.todoapp.model.TodoUserAssociation;
import com.andrio.todoapp.repository.TodoRepository;
import com.andrio.todoapp.repository.TodoUserAssociationRepository;
import com.andrio.todoapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TodoServiceTests {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TodoUserAssociationRepository todoUserAssociationRepository;

    @InjectMocks
    private TodoService todoService;

    private TodoUserDto todoUserDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        todoUserDto = new TodoUserDto(1L, "test.user", "test.user@email.com");
        when(kafkaTemplate.send(anyString(), any())).thenReturn(null);
        todoService = new TodoService(kafkaTemplate, todoRepository, todoUserAssociationRepository, userRepository);
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
        TodoUpdateDto todoUpdateDto = new TodoUpdateDto("Updated Name", "Updated Description", LocalDate.now(), Status.COMPLETED, 2, new ArrayList<>());
        Todo existingTodo = new Todo();
        existingTodo.setId(1L);
        TodoUserAssociation association = new TodoUserAssociation();
        association.setTodoUserId(1L);
        association.setTodoId(1L);
        existingTodo.setTodoUserAssociations(Collections.singletonList(association));

        when(todoRepository.findById(1L)).thenReturn(java.util.Optional.of(existingTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> i.getArguments()[0]);

        Todo result = todoService.updateTodo(1L, todoUserDto, todoUpdateDto);
        assertThat(result.getName()).isEqualTo(todoUpdateDto.getName());
    }

    @Test
    void updateTodoWithValidOwnerAndUserIdsUpdatesTodo() {
        // Setup
        Long todoId = 1L;
        Long userId = 1L;
        TodoUpdateDto todoUpdateDto = new TodoUpdateDto("Updated Name", "Updated Description", LocalDate.now(), Status.COMPLETED, 2, List.of(2L, 3L));
        Todo existingTodo = new Todo();
        existingTodo.setId(todoId);
        TodoUserAssociation ownerAssociation = new TodoUserAssociation();
        ownerAssociation.setTodoUserId(userId);
        ownerAssociation.setRole(Role.OWNER);
        existingTodo.setTodoUserAssociations(List.of(ownerAssociation));

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(existingTodo));
        when(userRepository.findUserIdsByIds(todoUpdateDto.getSharedWithUserIds())).thenReturn(todoUpdateDto.getSharedWithUserIds());
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> i.getArguments()[0]);
        when(todoUserAssociationRepository.findAllByTodoId(todoId)).thenReturn(new ArrayList<>());

        // Execute
        Todo result = todoService.updateTodo(todoId, todoUserDto, todoUpdateDto);

        // Verify
        assertThat(result.getName()).isEqualTo(todoUpdateDto.getName());
        assertThat(result.getDescription()).isEqualTo(todoUpdateDto.getDescription());
        assertThat(result.getStatus()).isEqualTo(todoUpdateDto.getStatus());
    }

    @Test
    void updateTodoWithInvalidSharedUserIdsThrowsException() {
        // Setup
        Long todoId = 1L;
        Long userId = 1L;
        TodoUpdateDto todoUpdateDto = new TodoUpdateDto("Updated Name", "Updated Description", LocalDate.now(), Status.COMPLETED, 2, List.of(99L));
        Todo existingTodo = new Todo();
        existingTodo.setId(todoId);
        TodoUserAssociation ownerAssociation = new TodoUserAssociation();
        ownerAssociation.setTodoUserId(userId);
        ownerAssociation.setRole(Role.OWNER);
        existingTodo.setTodoUserAssociations(List.of(ownerAssociation));

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(existingTodo));
        when(userRepository.findUserIdsByIds(todoUpdateDto.getSharedWithUserIds())).thenReturn(new ArrayList<>());

        // Execute & Verify
        assertThatThrownBy(() -> todoService.updateTodo(todoId, todoUserDto, todoUpdateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Some sharedWithUserIds are invalid");
    }

    @Test
    void updateTodoAsNonOwnerDoesNotUpdateAssociations() {
        // Setup
        Long todoId = 1L;
        Long userId = 2L; // Non-owner user ID
        TodoUpdateDto todoUpdateDto = new TodoUpdateDto("Updated Name", "Updated Description", LocalDate.now(), Status.COMPLETED, 2, List.of(3L));
        Todo existingTodo = new Todo();
        existingTodo.setId(todoId);
        TodoUserAssociation ownerAssociation = new TodoUserAssociation();
        ownerAssociation.setTodoUserId(1L); // Owner user ID
        ownerAssociation.setRole(Role.OWNER);
        TodoUserAssociation collaboratorAssociation = new TodoUserAssociation();
        collaboratorAssociation.setTodoUserId(2L); // Owner user ID
        collaboratorAssociation.setRole(Role.COLLABORATOR);
        existingTodo.setTodoUserAssociations(List.of(ownerAssociation, collaboratorAssociation));

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(existingTodo));
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.findUserIdsByIds(todoUpdateDto.getSharedWithUserIds())).thenReturn(todoUpdateDto.getSharedWithUserIds());

        todoUserDto.setId(2L);
        // Execute
        Todo result = todoService.updateTodo(todoId, todoUserDto, todoUpdateDto);

        // Verify
        assertThat(result).isNotNull();
        verify(todoUserAssociationRepository, never()).deleteAll(any());
        verify(todoUserAssociationRepository, never()).saveAll(any());
    }

    @Test
    void updateTodoRemovesUnsharedAssociationsAndAddsNewOnes() {
        // Setup
        Long todoId = 1L;
        Long userId = 1L;
        TodoUpdateDto todoUpdateDto = new TodoUpdateDto("Updated Name", "Updated Description", LocalDate.now(), Status.COMPLETED, 2, List.of(2L)); // Only user 2 is shared
        Todo existingTodo = new Todo();
        existingTodo.setId(todoId);
        TodoUserAssociation ownerAssociation = new TodoUserAssociation();
        ownerAssociation.setTodoUserId(userId);
        ownerAssociation.setRole(Role.OWNER);
        TodoUserAssociation collaboratorAssociation = new TodoUserAssociation();
        collaboratorAssociation.setTodoUserId(3L); // User 3 will be unshared
        collaboratorAssociation.setRole(Role.COLLABORATOR);
        existingTodo.setTodoUserAssociations(List.of(ownerAssociation, collaboratorAssociation));

        when(todoRepository.findById(todoId)).thenReturn(Optional.of(existingTodo));
        when(userRepository.findUserIdsByIds(todoUpdateDto.getSharedWithUserIds())).thenReturn(todoUpdateDto.getSharedWithUserIds());
        when(todoRepository.save(any(Todo.class))).thenAnswer(i -> i.getArguments()[0]);
        when(todoUserAssociationRepository.findAllByTodoId(todoId)).thenReturn(List.of(ownerAssociation)); // After update, only owner remains

        // Execute
        Todo result = todoService.updateTodo(todoId, todoUserDto, todoUpdateDto);

        // Verify
        verify(todoUserAssociationRepository).deleteAll(any()); // Verify unshared associations are removed
        verify(todoUserAssociationRepository).saveAll(any()); // Verify new associations are added
        assertThat(result.getTodoUserAssociations()).hasSize(1); // Only owner remains
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

        boolean result = todoService.deleteTodoIfOwner(1L, todoUserDto);
        assertThat(result).isTrue();
    }

    @Test
    void deleteTodoIfOwnerFailsForNonExistentTodo() {
        when(todoRepository.findById(1L)).thenReturn(java.util.Optional.empty());
        assertThat(todoService.deleteTodoIfOwner(1L, todoUserDto)).isFalse();
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

        boolean result = todoService.deleteTodoIfOwner(1L, todoUserDto);
        assertThat(result).isFalse();
    }

}

