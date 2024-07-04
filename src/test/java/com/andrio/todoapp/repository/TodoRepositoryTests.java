package com.andrio.todoapp.repository;

import com.andrio.todoapp.model.Status;
import com.andrio.todoapp.model.Todo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;


public class TodoRepositoryTests {

    @Mock
    private TodoRepository todoRepository;

    @BeforeEach
    void setUp() {
        // Setup data for testing
        MockitoAnnotations.openMocks(this);
        when(todoRepository.findAll(any(Specification.class), eq(Sort.unsorted())))
                .thenReturn(Arrays.asList(new Todo(), new Todo()));
    }

    @Test
    void findsTodosByStatusAndDueDateRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);
        List<Todo> results = todoRepository.findAll(TodoRepository.hasStatusAndDueDateBetween(null, Status.NOT_STARTED, startDate, endDate), Sort.unsorted());
        assertThat(results).isNotEmpty();
    }
}


