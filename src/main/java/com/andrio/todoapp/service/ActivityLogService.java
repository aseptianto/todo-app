package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.NotificationDto;
import com.andrio.todoapp.dto.TodoEventDto;
import com.andrio.todoapp.model.ActivityLog;
import com.andrio.todoapp.model.UsersActivityLogs;
import com.andrio.todoapp.repository.ActivityLogRepository;
import com.andrio.todoapp.repository.UsersActivityLogRepository;
import com.andrio.todoapp.repository.WebSocketUserRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UsersActivityLogRepository usersActivityLogRepository;

    @Autowired
    private WebSocketUserRegistry webSocketUserRegistry;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogService.class);


    @KafkaListener(topics = "todo-updates", groupId = "activity-log-group")
    public void listenTodoUpdates(String message) {
        try {
            TodoEventDto todoEventDto = new ObjectMapper().readValue(message, TodoEventDto.class);
            String fullText = String.format("%s %s %s (%d)", todoEventDto.getUserName(), todoEventDto.getAction(), todoEventDto.getTodoName(), todoEventDto.getTodoId());
            ActivityLog log = ActivityLog.builder()
                    .todoId(todoEventDto.getTodoId())
                    .action(todoEventDto.getAction())
                    .userId(todoEventDto.getUserId())
                    .fullText(fullText)
                    .build();
            logger.info("Received message: {}", message);
            ActivityLog savedLog = activityLogRepository.save(log);
            Long savedLogId = savedLog.getId();
            List<Long> associatedUserIds = todoEventDto.getAssociatedUserIds();
            storeUsersActivityLogs(savedLogId, associatedUserIds);

            // Send websocket notifications
            try {
                logger.info("Sending out websocket notification");
                NotificationDto notificationDto = NotificationDto.builder()
                        .message(fullText)
                        .todoId(todoEventDto.getTodoId())
                        .timestamp(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .build();
                ObjectMapper objectMapper = new ObjectMapper();
                String notificationJson = objectMapper.writeValueAsString(notificationDto);
                associatedUserIds.forEach(userId -> {
                    logger.info("Looking for {}", userId);
                    String sessionId = webSocketUserRegistry.getSessionIdByUserId(userId);
                    if (sessionId != null) {
                        logger.info("Sending notification to user {} and session {}", userId, sessionId);
                        try {
                            SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
                            headerAccessor.setSessionId(sessionId);
                            headerAccessor.setLeaveMutable(true);
                            messagingTemplate.convertAndSendToUser(sessionId, "/queue/updates", notificationJson, headerAccessor.getMessageHeaders());
                        } catch (Exception e) {
                            logger.error("Error sending websocket notification: {}", e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                logger.error("Error sending websocket notification: {}", e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error parsing message: {}", message);
        }
    }

    public void storeUsersActivityLogs(Long activityLogId, List<Long> userIds) {
        userIds.forEach(userId -> {
            UsersActivityLogs usersActivityLogs = UsersActivityLogs.builder()
                    .activityLogsId(activityLogId)
                    .userId(userId)
                    .build();
            usersActivityLogRepository.save(usersActivityLogs);
        });
    }
}