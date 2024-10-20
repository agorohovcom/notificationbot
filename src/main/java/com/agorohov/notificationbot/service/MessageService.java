package com.agorohov.notificationbot.service;

import com.agorohov.notificationbot.model.NotificationTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MessageService {
    private final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final TelegramApiService telegramApiService;
    private final NotificationTaskService notificationTaskService;

    public MessageService(TelegramApiService telegramApiService, NotificationTaskService notificationTaskService) {
        this.telegramApiService = telegramApiService;
        this.notificationTaskService = notificationTaskService;
    }

    /**
     * Отправляет пользователю с chatId приветственное сообщение с обращением по имени firstName.
     *
     * @param chatId    id пользователя в Telegram
     * @param firstName имя пользователя Telegram
     */
    public void responseToStartMessage(Long chatId, String firstName) {
        logger.info("Получено сообщение {} от пользователя с id={}", "\"/start\"", chatId);
        String startMessage = "Привет, " + firstName + "! " +
                "Чтобы установить напоминание, напиши сообщение в таком формате:\n" +
                "\"ДД.ММ.ГГГГ ЧЧ:ММ Текст сообщения\"";
        telegramApiService.sendMessage(chatId, startMessage);
    }

    /**
     * Метод работает по расписанию каждую минуту.
     * При вызове метод сохраняет в переменную currentMinute
     * текучее время с точностью до минуты, затем передаёт эту минуту как аргумент
     * в notificationTaskService.getCurrentMinuteTasks(currentMinute) и получает
     * список NotificationTask с временем отправки, совпадающим с currentMinute.
     * Сообщения из каждого полученного NotificationTask рассылаются получателям с указанными chatId.
     * Затем вызывается метод notificationTaskService.deleteAllInBatch(currentMinuteTasks), который инициализирует
     * удаление из БД всех записей, полученных вызовом notificationTaskService.getCurrentMinuteTasks(currentMinute).
     */
    @Scheduled(cron = "0 */1 * * * *")
    public void sendCurrentMinuteNotifications() {
        logger.info("Метод {} начал работу", "sendCurrentMinuteNotifications");

        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> currentMinuteTasks = notificationTaskService.getCurrentMinuteTasks(currentMinute);

        for (NotificationTask task : currentMinuteTasks) {
            telegramApiService.sendMessage(task.getChatId(), task.getMessageText());
        }

        logger.info("На текущую минуту отправлено {} напоминаний", currentMinuteTasks.size());

        notificationTaskService.deleteAllInBatch(currentMinuteTasks);
    }
}
