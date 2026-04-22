package com.collabsync.backend.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateMessage {
    private String eventType; // TASK_CREATED, TASK_UPDATED, TASK_DELETED
    private Object task;
    private Long projectId;
}