package com.agorohov.notificationbot.listener;

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

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() != null
                    && update.message().text() != null
                    && update.message().text().equals("/start")) {
                Long id = update.message().chat().id();
                String firstName = update.message().chat().firstName();
                SendMessage message = new SendMessage(id, "Привет, " + firstName + "!");
                SendResponse response = telegramBot.execute(message);
                if (!response.isOk()) {
                    logger.error("Failed to send message: {}", response.description());
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
