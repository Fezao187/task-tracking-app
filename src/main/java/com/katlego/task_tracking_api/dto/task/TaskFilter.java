package com.katlego.task_tracking_api.dto.task;

import com.katlego.task_tracking_api.domain.TaskStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class TaskFilter {
    private TaskStatus status;
    private Instant dueDateFrom;
    private Instant dueDateTo;
    private Long assignedUserId;
}
