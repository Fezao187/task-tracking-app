package com.katlego.task_tracking_api.dto.task;

import com.katlego.task_tracking_api.domain.TaskStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Instant dueDate;
    private Instant createdDate;
    private Long assignedUserId;
    private String assignedUsername;
}
