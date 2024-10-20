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

    private final TelegramApiService telegramApiService;
    private final NotificationTaskRepository repository;

    public NotificationTaskService(TelegramApiService telegramApiService, NotificationTaskRepository repository) {
        this.telegramApiService = telegramApiService;
        this.repository = repository;
    }

    /**
     * Сперва метод проверяет, соответствует ли userMessage пользователя формату "ДД.ММ.ГГГГ ЧЧ:ММ Текст сообщения".
     * Если не соответствует - отправляется сообщение об ошибке.
     * Если дата в userMessage меньше текущей даты, отправляется сообщение об ошибке.
     * Создаётся объект NotificationTask, данные сохраняются в БД.
     * Пользователь получает сообщение об успешном добавлении напоминания.
     *
     * @param chatId      - id пользователя в Telegram
     * @param userMessage - сообщение от пользователя
     */
    public void createNotification(Long chatId, String userMessage) {
        Matcher matcher;
        LocalDateTime notificationDate;
        String messageText;
        try {
            matcher = gerUserMessageMatcher(userMessage);
            notificationDate = parseLocalDateTime(matcher.group(1));
            messageText = matcher.group(3);
        } catch (RuntimeException e) {
            String exceptionString = "Ошибка: " + e.getMessage();
            telegramApiService.sendMessage(chatId, exceptionString);
            logger.error(exceptionString);
            return;
        }

        NotificationTask task = new NotificationTask();
        task.setChatId(chatId);
        task.setSendingTime(notificationDate);
        task.setMessageText(messageText);

        repository.save(task);
        logger.info("Напоминание сохранено: {}", task);

        String responseMessage = "Напоминание с текстом: \""
                + messageText
                + "\" установлено на дату и время: "
                + notificationDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        telegramApiService.sendMessage(chatId, responseMessage);
    }

    /**
     * Метод возвращает список объектов NotificationTask, у которых время в поле sendingTime
     * равно переданному параметру currentMinute.
     * @param currentMinute время с точностью до минуты
     * @return объектов NotificationTask, у которых время в поле sendingTime
     * равно переданному параметру currentMinute.
     */
    public List<NotificationTask> getCurrentMinuteTasks(LocalDateTime currentMinute) {
        return repository.getCurrentMinuteTasks(currentMinute);
    }

    /**
     * Инициализирует удаление всех записей, соответствующих сущностям из переданного списка currentMinuteTasks.
     * @param currentMinuteTasks список сущностей, соответствующие которым записи в БД должны быть удалены пакетно.
     */
    public void deleteAllInBatch(List<NotificationTask> currentMinuteTasks) {
        repository.deleteAllInBatch(currentMinuteTasks);
    }

    private Matcher gerUserMessageMatcher(String text) {
        String pattern = "(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(text);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Сообщение не соответствует формату " +
                    "\"ДД.ММ.ГГГГ ЧЧ:ММ Текст сообщения\"");
        }

        return matcher;
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
