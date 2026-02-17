package com.katlego.task_tracking_api.dto.task;

import com.katlego.task_tracking_api.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class TaskRequest {
    @NotBlank(message = "Title is required!")
    @Size(max = 200)
    private String title;
    private String description;
    private TaskStatus status;
    private Instant dueDate;
}