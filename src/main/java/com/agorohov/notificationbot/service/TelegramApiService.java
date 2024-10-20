package com.agorohov.notificationbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TelegramApiService {
    Logger logger = LoggerFactory.getLogger(TelegramApiService.class);

    private final TelegramBot telegramBot;

    public TelegramApiService(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
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
        if (response.isOk()) {
            logger.info("Сообщение отправлено пользователю id={}, текст=\"{}\"", chatId, messageText);
        } else {
            logger.error("Ошибка отправки сообщения: {}", response.description());
        }
    }
}
