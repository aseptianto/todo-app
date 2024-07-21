package com.andrio.todoapp.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class NotificationDto {
    private String message;
    private Long todoId;
    private Long timestamp;
}
