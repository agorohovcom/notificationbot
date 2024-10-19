package com.agorohov.notificationbot.service;

import com.agorohov.notificationbot.model.NotificationTask;
import com.agorohov.notificationbot.repository.NotificationTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {

    Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);

    private final NotificationTaskRepository repository;

    public NotificationTaskService(NotificationTaskRepository repository) {
        this.repository = repository;
    }

    public String createNotification(Long chatId, String text) {
        LocalDateTime notificationDate;
        String message;
        try {
            checkNotificationTextFormat(text);
            notificationDate = parseLocalDateTime(text.substring(0, 16));
            message = text.substring(17).trim();
        } catch (RuntimeException e) {
            return "Ошибка: " + e.getMessage();
        }

        NotificationTask task = new NotificationTask();
        task.setChatId(chatId);
        task.setSendingTime(notificationDate);
        task.setMessageText(message);

        repository.save(task);
        return "Напоминание с текстом:\n\""
                + message
                + "\"\nустановлено на дату и время:\n"
                + notificationDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    private void checkNotificationTextFormat(String text) {
        String pattern = "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(text);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Сообщение не соответствует формату\n\"ДД.ММ.ГГГГ ЧЧ:ММ Текст сообщения\"");
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
