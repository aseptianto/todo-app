package com.andrio.todoapp.dto;

import com.andrio.todoapp.model.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class TodoCreateDto {
    @NotEmpty(message = "Name is required")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotNull(message = "Status is required")
    private Status status;

    @NotNull(message = "Priority is required")
    private Integer priority;
}
