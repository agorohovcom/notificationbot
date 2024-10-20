package com.agorohov.notificationbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramApiServiceTest {

    @InjectMocks
    private TelegramApiService out;

    @Mock
    private TelegramBot telegramBotMock;

    SendResponse sendResponse;

    @BeforeEach
    void setUp() {
        sendResponse = mock(SendResponse.class);
    }

    @Test
    void sendMessageSuccessTest() {
        when(sendResponse.isOk()).thenReturn(true);
        when(telegramBotMock.execute(any(SendMessage.class))).thenReturn(sendResponse);
        out.sendMessage(1L, "Test text");
        verify(telegramBotMock, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void sendMessageFailureTest() {
        when(sendResponse.isOk()).thenReturn(false);
        when(telegramBotMock.execute(any(SendMessage.class))).thenReturn(sendResponse);
        out.sendMessage(1L, "Test text");
        verify(telegramBotMock, times(1)).execute(any(SendMessage.class));
    }
}