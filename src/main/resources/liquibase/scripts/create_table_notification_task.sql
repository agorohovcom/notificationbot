-- liquibase formatted sql

-- changeset agorohov:1
CREATE TABLE notification_task (
id SERIAL PRIMARY KEY,
chat_id BIGINT,
message_text TEXT,
sending_time TIMESTAMP WITH TIME ZONE
);