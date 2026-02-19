package com.katlego.task_tracking_api.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskAssignRequest {
    @NotNull(message = "assignedUserId is required")
    private Long assignedUserId;
}
