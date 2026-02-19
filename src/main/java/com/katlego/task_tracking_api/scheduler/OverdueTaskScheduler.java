package com.katlego.task_tracking_api.scheduler;

import com.katlego.task_tracking_api.domain.Task;
import com.katlego.task_tracking_api.domain.TaskStatus;
import com.katlego.task_tracking_api.repository.TaskRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@Slf4j
public class OverdueTaskScheduler {

    private final TaskRepository taskRepository;

    public OverdueTaskScheduler(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    @Scheduled(cron = "${schedule.overdue-task-schedule-time:0 0 * * * *}")
    public void markOverdueTasks() {
        log.info("Starting overdue task check");

        int updated = taskRepository.markTasksAsOverdue(
                Instant.now(),
                List.of(TaskStatus.NEW, TaskStatus.IN_PROGRESS, TaskStatus.DELAYED),
                TaskStatus.OVERDUE
        );

        log.info("Marked {} task(s) as OVERDUE", updated);
    }
}
