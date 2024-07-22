package com.andrio.todoapp.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TodoEventDto {
    private String userName;
    private Long userId;
    private String todoName;
    private Long todoId;
    private String action;
    private List<Long> associatedUserIds;
}