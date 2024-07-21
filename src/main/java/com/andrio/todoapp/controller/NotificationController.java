package com.andrio.todoapp.controller;
import com.andrio.todoapp.model.NotificationMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationController {

    @MessageMapping("/notify")
    @SendTo("/topic/updates")
    public NotificationMessage send(NotificationMessage message) {
        return message;
    }
}
