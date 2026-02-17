package com.katlego.task_tracking_api.service;

import com.katlego.task_tracking_api.common.AuthenticatedUserComponent;
import com.katlego.task_tracking_api.domain.Task;
import com.katlego.task_tracking_api.dto.task.CreateTaskRequest;
import com.katlego.task_tracking_api.dto.task.TaskResponse;
import com.katlego.task_tracking_api.mapper.TaskMapper;
import com.katlego.task_tracking_api.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final AuthService authService;
    private final TaskRepository taskRepository;
    private final AuthenticatedUserComponent authenticatedUserComponent;
    private final TaskMapper taskMapper;

    public TaskService(AuthService authService, TaskRepository taskRepository, AuthenticatedUserComponent authenticatedUserComponent, TaskMapper taskMapper) {
        this.authService = authService;
        this.taskRepository = taskRepository;
        this.authenticatedUserComponent = authenticatedUserComponent;
        this.taskMapper = taskMapper;
    }

    public TaskResponse createTask(CreateTaskRequest request){
        if(!authenticatedUserComponent.isAdmin()){
            throw new AccessDeniedException("Only admins can create users");
        }

        Task newTask = taskMapper.toTaskFromRequest(request);

        return taskMapper.toTaskResponseFromModel(newTask);
    }
}
