package com.katlego.task_tracking_api.service;

import com.katlego.task_tracking_api.dto.task.CreateTaskRequest;
import com.katlego.task_tracking_api.dto.task.TaskResponse;
import com.katlego.task_tracking_api.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final AuthService authService;
    private final TaskRepository taskRepository;

    public TaskService(AuthService authService, TaskRepository taskRepository) {
        this.authService = authService;
        this.taskRepository = taskRepository;
    }

    public TaskResponse createTask(CreateTaskRequest request){

    }
}
