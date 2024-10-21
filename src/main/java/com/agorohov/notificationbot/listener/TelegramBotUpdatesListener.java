package com.agorohov.notificationbot.listener;

import com.agorohov.notificationbot.service.MessageService;
import com.agorohov.notificationbot.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private final MessageService messageService;

    public TelegramBotUpdatesListener(
            TelegramBot telegramBot,
            NotificationTaskService notificationTaskService,
            MessageService messageService
    ) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
        this.messageService = messageService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Получен объект update: {}", update);
            if (update.message() != null && update.message().text() != null) {
                Long chatId = update.message().chat().id();
                String userMessage = update.message().text();
                if (userMessage.equals("/start")) {
                    String firstName = update.message().chat().firstName();
                    messageService.responseToStartMessage(chatId, firstName);
                } else {
                    notificationTaskService.createNotification(chatId, userMessage);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
