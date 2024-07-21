package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.TodoEventDto;
import com.andrio.todoapp.model.ActivityLog;
import com.andrio.todoapp.model.UsersActivityLogs;
import com.andrio.todoapp.repository.ActivityLogRepository;
import com.andrio.todoapp.repository.UsersActivityLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private UsersActivityLogRepository usersActivityLogRepository;

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogService.class);


    @KafkaListener(topics = "todo-updates", groupId = "activity-log-group")
    public void listenTodoUpdates(String message) {
        try {
            TodoEventDto todoEventDto = new ObjectMapper().readValue(message, TodoEventDto.class);
            ActivityLog log = ActivityLog.builder()
                    .todoId(todoEventDto.getTodoId())
                    .action(todoEventDto.getAction())
                    .userId(todoEventDto.getUserId())
                    .build();
            logger.info("Received message: {}", message);
            ActivityLog savedLog = activityLogRepository.save(log);
            Long savedLogId = savedLog.getId();
            storeUsersActivityLogs(savedLogId, todoEventDto.getAssociatedUserIds());
        } catch (Exception e) {
            logger.error("Error parsing message: {}", message);
        }
    }

    private void storeUsersActivityLogs(Long activityLogId, List<Long> userIds) {
        userIds.forEach(userId -> {
            UsersActivityLogs usersActivityLogs = UsersActivityLogs.builder()
                    .activityLogsId(activityLogId)
                    .userId(userId)
                    .build();
            usersActivityLogRepository.save(usersActivityLogs);
        });
    }
}