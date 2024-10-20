package com.agorohov.notificationbot.service;

import com.agorohov.notificationbot.model.NotificationTask;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
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

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;

    public MessageService(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    /**
     * Отправляет пользователю с chatId приветственное сообщение с обращением по имени firstName.
     *
     * @param chatId    id пользователя в Telegram
     * @param firstName имя пользователя Telegram
     */
    public void responseToStartMessage(Long chatId, String firstName) {
        String startMessage = "Привет, " + firstName + "! " +
                "Чтобы установить напоминание, напиши сообщение в таком формате:\n" +
                "\"ДД.ММ.ГГГГ ЧЧ:ММ Текст сообщения\"";
        sendMessage(chatId, startMessage);
    }

    /**
     * Отправляет пользователю с переданным chatId сообщение с текстом messageText.
     *
     * @param chatId      id пользователя в Telegram
     * @param messageText текст отправляемого сообщения
     */
    public void sendMessage(Long chatId, String messageText) {
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        SendResponse response = telegramBot.execute(sendMessage);
        if (!response.isOk()) {
            logger.error("Ошибка отправки сообщения: {}", response.description());
        }
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
        LocalDateTime currentMinute = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> currentMinuteTasks = notificationTaskService.getCurrentMinuteTasks(currentMinute);

        for (NotificationTask task : currentMinuteTasks) {
            sendMessage(task.getChatId(), task.getMessageText());
        }

        notificationTaskService.deleteAllInBatch(currentMinuteTasks);
    }
}
