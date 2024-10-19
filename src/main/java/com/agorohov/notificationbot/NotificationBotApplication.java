package com.agorohov.notificationbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NotificationBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationBotApplication.class, args);
    }
}