package com.andrio.todoapp.service;

import com.andrio.todoapp.dto.TodoEventDto;
import com.andrio.todoapp.dto.TodoUserDto;
import com.andrio.todoapp.model.ActivityLog;
import com.andrio.todoapp.model.UsersActivityLogs;
import com.andrio.todoapp.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


public class ActivityLogServiceTests {

    private TodoEventDto todoEventDto;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @InjectMocks
    private ActivityLogService activityLogService;

    @Mock
    private UsersActivityLogRepository usersActivityLogRepository;

    @Mock
    private WebSocketUserRegistry webSocketUserRegistry;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listenTodoUpdatesHandlesInvalidMessageGracefully() {
        String invalidMessage = "invalid json";

        activityLogService.listenTodoUpdates(invalidMessage);

    }

    @Test
    @SneakyThrows
    void listenTodoUpdatesStoresActivityLogCorrectly() {
        String validMessage = "{\"userId\":1,\"userName\":\"testUser\",\"todoId\":10,\"todoName\":\"Test Todo\",\"action\":\"updated\",\"associatedUserIds\":[2,3]}";
        todoEventDto = new ObjectMapper().readValue(validMessage, TodoEventDto.class);
        when(activityLogRepository.save(any(ActivityLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        activityLogService.listenTodoUpdates(validMessage);

        verify(activityLogRepository, times(1)).save(any(ActivityLog.class));
    }

    @Test
    @SneakyThrows
    void listenTodoUpdatesSendsNotificationWithCorrectFormat() {
        String validMessage = "{\"userId\":1,\"userName\":\"testUser\",\"todoId\":10,\"todoName\":\"Test Todo\",\"action\":\"deleted\",\"associatedUserIds\":[2,3]}";
        todoEventDto = new ObjectMapper().readValue(validMessage, TodoEventDto.class);
        ArgumentCaptor<String> notificationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(messagingTemplate).convertAndSend(eq("/topic/updates"), notificationCaptor.capture());

        ActivityLog mockActivityLog = new ActivityLog();
        mockActivityLog.setId(1L);
        when(activityLogRepository.save(any())).thenAnswer((Answer<ActivityLog>) invocation -> {
            ActivityLog argument = invocation.getArgument(0);
            argument.setId(mockActivityLog.getId());
            return argument;
        });
        when(webSocketUserRegistry.getSessionIdByUserId(2L)).thenReturn("session123");

        activityLogService.listenTodoUpdates(validMessage);

//        verify(messagingTemplate).convertAndSend(eq("/topic/updates"), notificationCaptor.capture());
        verify(messagingTemplate).convertAndSendToUser(sessionIdCaptor.capture(), destinationCaptor.capture(), messageCaptor.capture());
//        String sentNotification = notificationCaptor.getValue();
        String capturedSessionId = sessionIdCaptor.getValue();
        String capturedDestination = destinationCaptor.getValue();
        String capturedMessage = messageCaptor.getValue();
        assertTrue(capturedSessionId.contains("session123"));
        assertTrue(capturedDestination.contains("/topic/updates"));
        assertTrue(capturedMessage.contains("\"todoId\":10"));
    }

    @Test
    void storeUsersActivityLogsStoresLogsForAllUserIds() {
        Long activityLogId = 1L;
        List<Long> userIds = Arrays.asList(2L, 3L, 4L);
        when(usersActivityLogRepository.save(any(UsersActivityLogs.class))).thenAnswer(invocation -> invocation.getArgument(0));

        activityLogService.storeUsersActivityLogs(activityLogId, userIds);

        verify(usersActivityLogRepository, times(userIds.size())).save(any(UsersActivityLogs.class));
    }
}
