package com.agorohov.notificationbot.scheduler;

import com.agorohov.notificationbot.model.NotificationTask;
import com.agorohov.notificationbot.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class NotificationSender {

    Logger logger = LoggerFactory.getLogger(NotificationSender.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskService service;

    public NotificationSender(TelegramBot telegramBot, NotificationTaskService service) {
        this.telegramBot = telegramBot;
        this.service = service;
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void sendCurrentMinuteNotifications() {
        LocalDateTime currentMinute = LocalDateTime
                .now()
                .truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> currentMinuteTasks = service.getCurrentMinuteTasks(currentMinute);
        for (NotificationTask task : currentMinuteTasks) {
            SendResponse response = telegramBot.execute(new SendMessage(task.getChatId(), task.getMessageText()));
            if (!response.isOk()) {
                logger.error("Failed to send message: {}", response.description());
            }
            service.deleteAllInBatch(currentMinuteTasks);
        }
    }

}
