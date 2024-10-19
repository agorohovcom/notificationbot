package com.agorohov.notificationbot.repository;

import com.agorohov.notificationbot.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    @Query(
            value = "SELECT * FROM notification_task WHERE DATE_TRUNC('minute', sending_time) = :current_minute",
            nativeQuery = true
    )
    List<NotificationTask> getCurrentMinuteTasks(@Param("current_minute") LocalDateTime currentMinute);
}
