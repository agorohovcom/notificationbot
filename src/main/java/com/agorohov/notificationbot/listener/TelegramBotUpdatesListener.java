package com.agorohov.notificationbot.listener;

import com.agorohov.notificationbot.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    private final NotificationTaskService service;

    public TelegramBotUpdatesListener(NotificationTaskService service) {
        this.service = service;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() != null && update.message().text() != null) {
                String userMessage = update.message().text();
                Long chatId = update.message().chat().id();
                SendMessage sendMessage;
                if (update.message().text().equals("/start")) {
                    String firstName = update.message().chat().firstName();
                    sendMessage = new SendMessage(chatId, "Привет, " + firstName + "! " +
                            "Чтобы установить напоминание, напиши сообщение в таком формате:\n" +
                            "\"ДД.ММ.ГГГГ ЧЧ:ММ Текст сообщения\"");
                } else {
                    sendMessage = new SendMessage(
                            chatId,
                            service.createNotification(chatId, userMessage)
                    );
                }
                SendResponse response = telegramBot.execute(sendMessage);
                if (!response.isOk()) {
                    logger.error("Failed to send message: {}", response.description());
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
