package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.TodoCreateDto;
import com.andrio.todoapp.dto.TodoEventDto;
import com.andrio.todoapp.dto.TodoUpdateDto;
import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.model.*;
import com.andrio.todoapp.repository.TodoRepository;
import com.andrio.todoapp.repository.TodoUserAssociationRepository;
import com.andrio.todoapp.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoUserAssociationRepository todoUserAssociationRepository;
    private final Set<String> allowedSortFields = Set.of("dueDate", "status", "name", "priority");
    private final UserRepository userRepository;
    private static final String KAFKA_UPDATE_TOPIC = "todo-updates";

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public TodoService(KafkaTemplate<String, String> kafkaTemplate, TodoRepository todoRepository, TodoUserAssociationRepository todoUserAssociationRepository, UserRepository userRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.todoRepository = todoRepository;
        this.todoUserAssociationRepository = todoUserAssociationRepository;
        this.userRepository = userRepository;
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
    public Todo updateTodo(Long todoId, TodoUserDto todoUserDto, TodoUpdateDto todoUpdateDto) {
        Todo todo = todoRepository.findById(todoId).orElseThrow(() -> new EntityNotFoundException("Todo not found"));
        Long userId = todoUserDto.getId();

        TodoUserAssociation ownerAssociation = todo.getTodoUserAssociations().stream()
                .filter(a -> a.getTodoUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("User not associated with todo"));

        todo.setName(todoUpdateDto.getName());
        todo.setDescription(todoUpdateDto.getDescription());
        todo.setDueDate(todoUpdateDto.getDueDate());
        todo.setStatus(todoUpdateDto.getStatus());
        todo.setPriority(todoUpdateDto.getPriority().toString());
        todo.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        Todo firstUpdateResult = todoRepository.save(todo);

        boolean isOwner = ownerAssociation.getRole() == Role.OWNER;
        if (isOwner) {
            // check if all sharedWithUserIds are real user IDs
            List<Long> realUserIds = userRepository.findUserIdsByIds(todoUpdateDto.getSharedWithUserIds());
            if (realUserIds.size() != todoUpdateDto.getSharedWithUserIds().size()) {
                throw new EntityNotFoundException("Some sharedWithUserIds are invalid");
            }
            // handle TodoUserAssociation mapping. Remove those not in sharedWithUserIds and add new ones
            List<TodoUserAssociation> currentAssociations = todo.getTodoUserAssociations();
            // filter out the user from sharedWithUserIds
            List<Long> sharedWithUserIds = todoUpdateDto.getSharedWithUserIds().stream()
                    .filter(id -> !id.equals(userId))
                    .toList();;
            List<TodoUserAssociation> toRemove = currentAssociations.stream()
                    .filter(association -> !sharedWithUserIds.contains(association.getTodoUserId()) && association.getRole() != Role.OWNER)
                    .toList();

            todoUserAssociationRepository.deleteAll(toRemove);

            Set<Long> currentAssociationsUserIds = currentAssociations.stream().map(TodoUserAssociation::getTodoUserId).collect(Collectors.toSet());
            List<Long> toAdd = sharedWithUserIds.stream().filter(id -> !currentAssociationsUserIds.contains(id)).toList();

            List<TodoUserAssociation> newAssociations = toAdd.stream()
                    .map(toAddUserId -> TodoUserAssociation.builder()
                            .todoId(todoId)
                            .todoUserId(toAddUserId)
                            .role(Role.COLLABORATOR)
                            .createdAt(new Timestamp(System.currentTimeMillis()))
                            .build())
                    .toList();
            todoUserAssociationRepository.saveAll(newAssociations);

            List<TodoUserAssociation> updatedAssociations = todoUserAssociationRepository.findAllByTodoId(todoId);
            firstUpdateResult.setTodoUserAssociations(updatedAssociations);
        }
        // send kafka event
        try {
            List<Long> associatedUserIds = todo.getTodoUserAssociations().stream()
                    .map(TodoUserAssociation::getTodoUserId)
                    .filter(id -> !id.equals(userId))
                    .toList();
            TodoEventDto eventDto = TodoEventDto.builder()
                    .userId(userId)
                    .userName(todoUserDto.getName())
                    .todoId(todoId)
                    .todoName(todo.getName())
                    .action("updated")
                    .associatedUserIds(associatedUserIds)
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            String eventJson = null;
            eventJson = objectMapper.writeValueAsString(eventDto);
            kafkaTemplate.send(KAFKA_UPDATE_TOPIC, eventJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return firstUpdateResult;
    }

    @Transactional
    public boolean deleteTodoIfOwner(Long todoId, TodoUserDto todoUserDto) {
        Todo todo = todoRepository.findById(todoId).orElse(null);
        Long userId = todoUserDto.getId();
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
            // send kafka event
            try {
                List<Long> associatedUserIds = todo.getTodoUserAssociations().stream()
                        .map(TodoUserAssociation::getTodoUserId)
                        .filter(id -> !id.equals(userId))
                        .toList();
                TodoEventDto eventDto = TodoEventDto.builder()
                        .userId(userId)
                        .userName(todoUserDto.getName())
                        .todoId(todoId)
                        .todoName(todo.getName())
                        .action("deleted")
                        .associatedUserIds(associatedUserIds)
                        .build();

                ObjectMapper objectMapper = new ObjectMapper();
                String eventJson = null;
                eventJson = objectMapper.writeValueAsString(eventDto);
                kafkaTemplate.send(KAFKA_UPDATE_TOPIC, eventJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }
}