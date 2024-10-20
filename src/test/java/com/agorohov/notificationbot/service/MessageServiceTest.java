package com.agorohov.notificationbot.service;

import com.agorohov.notificationbot.model.NotificationTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @InjectMocks
    private MessageService out;

    @Mock
    private TelegramApiService telegramApiServiceMock;
    @Mock
    private NotificationTaskService notificationTaskServiceMock;

    @Test
    void responseToStartMessageTest() {
        out.responseToStartMessage(1L, "Test text");

        verify(telegramApiServiceMock, times(1)).sendMessage(anyLong(), anyString());
    }

    @Test
    void sendCurrentMinuteNotificationsTest() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = List.of(
                new NotificationTask(null, 1L, "Test text 1", currentMinute),
                new NotificationTask(null, 2L, "Test text 2", currentMinute),
                new NotificationTask(null, 3L, "Test text 3", currentMinute),
                new NotificationTask(null, 4L, "Test text 4", currentMinute),
                new NotificationTask(null, 5L, "Test text 5", currentMinute)
        );

        when(notificationTaskServiceMock.getCurrentMinuteTasks(currentMinute)).thenReturn(tasks);

        out.sendCurrentMinuteNotifications();

        verify(telegramApiServiceMock, times(tasks.size())).sendMessage(anyLong(), anyString());
        verify(notificationTaskServiceMock, times(1)).deleteAllInBatch(tasks);
    }
}