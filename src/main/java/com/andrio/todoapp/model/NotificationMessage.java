package com.andrio.todoapp.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class NotificationMessage {
    // Getters and Setters
    private String content;

    public NotificationMessage() {
    }

    public NotificationMessage(String content) {
        this.content = content;
    }

}