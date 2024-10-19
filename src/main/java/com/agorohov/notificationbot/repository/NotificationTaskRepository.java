package com.agorohov.notificationbot.repository;

import com.agorohov.notificationbot.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
}
