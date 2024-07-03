package com.andrio.todoapp.model;

import lombok.Getter;

@Getter
public enum Status {
    NOT_STARTED(0),
    IN_PROGRESS(1),
    COMPLETED(2);
    
    private final int value;
    
    Status(int value) {
        this.value = value;
    }

    public static Status fromValue(int value) {
        for (Status status : Status.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status value: " + value);
    }
}
