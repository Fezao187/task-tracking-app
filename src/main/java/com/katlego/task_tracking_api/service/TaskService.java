package com.katlego.task_tracking_api.service;

import com.katlego.task_tracking_api.common.AuthenticatedUserComponent;
import com.katlego.task_tracking_api.domain.Task;
import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.dto.task.TaskRequest;
import com.katlego.task_tracking_api.dto.task.TaskResponse;
import com.katlego.task_tracking_api.exception.ResourceNotFoundException;
import com.katlego.task_tracking_api.mapper.TaskMapper;
import com.katlego.task_tracking_api.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

    public TaskResponse createTask(TaskRequest request) {
        if (!authenticatedUserComponent.isAdmin()) {
            throw new AccessDeniedException("Only admins can create tasks");
        }

        Task newTask = taskMapper.toTaskFromRequest(request);

        return taskMapper.toTaskResponseFromModel(taskRepository.save(newTask));
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request) {
        if (!authenticatedUserComponent.isAdmin()) {
            throw new AccessDeniedException("Only admins can update tasks");
        }
        Task updateTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + taskId + ", not found."));

        updateTask.setTitle(request.getTitle());
        updateTask.setStatus(request.getStatus());
        updateTask.setDescription(request.getDescription());
        updateTask.setDueDate(request.getDueDate());

        return taskMapper.toTaskResponseFromModel(taskRepository.save(updateTask));
    }

    public TaskResponse getTaskById(Long taskId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + taskId + ", not found."));

        return taskMapper.toTaskResponseFromModel(task);
    }

    public List<TaskResponse> getAllMyAssignedTasks(){
        User user = authenticatedUserComponent.getCurrentLoggedInUser();

        List<Task> myTasks = taskRepository.findTaskByAssignedUser(user.getId())
                .orElseThrow(()->new ResourceNotFoundException("The logged in user currently has no tasks"));

        return myTasks
                .stream()
                .map(taskMapper::toTaskResponseFromModel)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getAllTasks(){
        if (!authenticatedUserComponent.isAdmin()) {
            throw new AccessDeniedException("Only admins can see all tasks");
        }

        List<Task> allTasks = taskRepository.findAll();

        if (allTasks.isEmpty()){
            throw new ResourceNotFoundException("There are no tasks created.");
        }

        return allTasks
                .stream()
                .map(taskMapper::toTaskResponseFromModel)
                .collect(Collectors.toList());
    }

    public String deleteTaskById(Long taskId){
        if (!authenticatedUserComponent.isAdmin()) {
            throw new AccessDeniedException("Only admins can delete tasks tasks");
        }

        taskRepository.deleteById(taskId);

        return "Successfully deleted!";
    }
}
