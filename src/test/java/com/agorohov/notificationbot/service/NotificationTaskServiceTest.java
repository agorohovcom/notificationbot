package com.agorohov.notificationbot.service;

import com.agorohov.notificationbot.model.NotificationTask;
import com.agorohov.notificationbot.repository.NotificationTaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTaskServiceTest {

    @InjectMocks
    private NotificationTaskService out;

    @Mock
    private TelegramApiService telegramApiServiceMock;
    @Mock
    private NotificationTaskRepository notificationTaskRepositoryMock;

    LocalDateTime currentMinutePlus1;
    List<NotificationTask> tasks;

    @BeforeEach
    void setUp() {
        currentMinutePlus1 = LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
        tasks = List.of(
                new NotificationTask(null, 1L, "Test text 1", currentMinutePlus1),
                new NotificationTask(null, 2L, "Test text 2", currentMinutePlus1),
                new NotificationTask(null, 3L, "Test text 3", currentMinutePlus1),
                new NotificationTask(null, 4L, "Test text 4", currentMinutePlus1),
                new NotificationTask(null, 5L, "Test text 5", currentMinutePlus1)
        );
    }

    @AfterEach
    void tearDown() {
        currentMinutePlus1 = null;
        tasks = null;
    }

    @Test
    void createNotificationCorrectTest() {
        Long chatId = 1L;
        String textMessage = "Test text";
        String dateTime = currentMinutePlus1.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        NotificationTask expectedNotificationTask = new NotificationTask();
        expectedNotificationTask.setChatId(chatId);
        expectedNotificationTask.setMessageText(textMessage);
        expectedNotificationTask.setSendingTime(currentMinutePlus1);

        out.createNotification(chatId, dateTime + " " + textMessage);

        verify(notificationTaskRepositoryMock, times(1)).save(expectedNotificationTask);
        verify(telegramApiServiceMock, times(1)).sendMessage(anyLong(), anyString());
    }

    @Test
    void createNotificationIncorrectDateFormatTest() {
        Long chatId = 1L;
        String textMessage = "Test text";
        String incorrectFormatDateTime = currentMinutePlus1.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"));

        NotificationTask expectedNotificationTask = new NotificationTask();
        expectedNotificationTask.setChatId(chatId);
        expectedNotificationTask.setMessageText(textMessage);
        expectedNotificationTask.setSendingTime(currentMinutePlus1);

        out.createNotification(chatId, incorrectFormatDateTime + " " + textMessage);

        verify(notificationTaskRepositoryMock, times(0)).save(expectedNotificationTask);
        verify(telegramApiServiceMock, times(1)).sendMessage(anyLong(), anyString());
    }

    @Test
    void createNotificationIncorrectPattern() {
        Long chatId = 1L;
        String textMessage = "Test text";
        String incorrectFormatDateTime = currentMinutePlus1.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm"));

        NotificationTask expectedNotificationTask = new NotificationTask();
        expectedNotificationTask.setChatId(chatId);
        expectedNotificationTask.setMessageText(textMessage);
        expectedNotificationTask.setSendingTime(currentMinutePlus1);

        out.createNotification(chatId, incorrectFormatDateTime + textMessage);

        verify(notificationTaskRepositoryMock, times(0)).save(expectedNotificationTask);
        verify(telegramApiServiceMock, times(1)).sendMessage(anyLong(), anyString());
    }

    @Test
    void getCurrentMinuteTasksTest() {
        when(notificationTaskRepositoryMock.getCurrentMinuteTasks(currentMinutePlus1)).thenReturn(tasks);
        List<NotificationTask> actual = out.getCurrentMinuteTasks(currentMinutePlus1);
        verify(notificationTaskRepositoryMock, times(1)).getCurrentMinuteTasks(currentMinutePlus1);
        assertIterableEquals(tasks, actual);
    }

    @Test
    void getCurrentMinuteTasksEmptyListTest() {
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> emptyList = List.of();
        when(notificationTaskRepositoryMock.getCurrentMinuteTasks(currentMinute)).thenReturn(emptyList);
        List<NotificationTask> actual = out.getCurrentMinuteTasks(currentMinute);
        verify(notificationTaskRepositoryMock, times(1)).getCurrentMinuteTasks(currentMinute);
        assertIterableEquals(emptyList, actual);
    }

    @Test
    void deleteAllInBatchTest() {
        out.deleteAllInBatch(tasks);
        verify(notificationTaskRepositoryMock, times(1)).deleteAllInBatch(tasks);
    }

    @Test
    void deleteAllInBatchEmptyListTest() {
        List<NotificationTask> emptyList = List.of();
        out.deleteAllInBatch(emptyList);
        verify(notificationTaskRepositoryMock, times(1)).deleteAllInBatch(emptyList);
    }
}