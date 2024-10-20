package com.agorohov.notificationbot.service;

import com.agorohov.notificationbot.model.NotificationTask;
import com.agorohov.notificationbot.repository.NotificationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {
    private final Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);

    private final NotificationTaskRepository repository;

    public NotificationTaskService(NotificationTaskRepository repository) {
        this.repository = repository;
    }

    // Сделать вместо substring и trim Matcher.group(int group)
    public String createNotification(Long chatId, String text) {
        LocalDateTime notificationDate;
        String messageText;
        String resultText;
        try {
            checkNotificationTextFormat(text);
            notificationDate = parseLocalDateTime(text.substring(0, 16));
            messageText = text.substring(17).trim();
        } catch (RuntimeException e) {
            return "Ошибка: " + e.getMessage();
        }

        NotificationTask task = new NotificationTask();
        task.setChatId(chatId);
        task.setSendingTime(notificationDate);
        task.setMessageText(messageText);

        repository.save(task);
        return "Напоминание с текстом:\n\""
                + messageText
                + "\"\nустановлено на дату и время:\n"
                + notificationDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public List<NotificationTask> getCurrentMinuteTasks(LocalDateTime currentMinute) {
        return repository.getCurrentMinuteTasks(currentMinute);
    }

    public void deleteAllInBatch(List<NotificationTask> currentMinuteTasks) {
        repository.deleteAllInBatch(currentMinuteTasks);
    }

    private void checkNotificationTextFormat(String text) {
        String pattern = "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(text);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Сообщение не соответствует формату\n" +
                    "\"ДД.ММ.ГГГГ ЧЧ:ММ Текст сообщения\"");
        }
    }

    private LocalDateTime parseLocalDateTime(String stringDate) {
        LocalDateTime notificationDate = LocalDateTime.parse(
                stringDate,
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        );
        if (notificationDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Переданная дата уже в прошлом");
        }
        return notificationDate;
    }
}
