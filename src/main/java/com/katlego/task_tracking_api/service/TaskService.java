package com.katlego.task_tracking_api.service;

import com.katlego.task_tracking_api.common.AuthenticatedUserComponent;
import com.katlego.task_tracking_api.domain.Task;
import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.dto.task.TaskAssignRequest;
import com.katlego.task_tracking_api.dto.task.TaskDeleteResponse;
import com.katlego.task_tracking_api.dto.task.TaskFilter;
import com.katlego.task_tracking_api.dto.task.TaskRequest;
import com.katlego.task_tracking_api.dto.task.TaskResponse;
import com.katlego.task_tracking_api.exception.ResourceNotFoundException;
import com.katlego.task_tracking_api.mapper.TaskMapper;
import com.katlego.task_tracking_api.repository.TaskRepository;
import com.katlego.task_tracking_api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final UserRepository userRepository;

    public TaskService(AuthService authService, TaskRepository taskRepository, AuthenticatedUserComponent authenticatedUserComponent, TaskMapper taskMapper, UserRepository userRepository) {
        this.authService = authService;
        this.taskRepository = taskRepository;
        this.authenticatedUserComponent = authenticatedUserComponent;
        this.taskMapper = taskMapper;
        this.userRepository = userRepository;
    }

    public TaskResponse createTask(TaskRequest request) {

        Task newTask = taskMapper.toTaskFromRequest(request);

        return taskMapper.toTaskResponseFromModel(taskRepository.save(newTask));
    }

    public TaskResponse updateTask(Long taskId, TaskRequest request) {
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
        List<Task> allTasks = taskRepository.findAll();

        if (allTasks.isEmpty()){
            throw new ResourceNotFoundException("There are no tasks created.");
        }

        return allTasks
                .stream()
                .map(taskMapper::toTaskResponseFromModel)
                .collect(Collectors.toList());
    }

    public TaskDeleteResponse deleteTaskById(Long taskId){

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + taskId + ", not found."));

        taskRepository.delete(task);

        return new TaskDeleteResponse("Task successfully deleted");
    }

    public TaskResponse assignTask(Long taskId, TaskAssignRequest request) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id: " + taskId + ", not found."));

        User assignedUser = userRepository.findById(request.getAssignedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + request.getAssignedUserId() + ", not found."));

        task.setAssignedUser(assignedUser);

        return taskMapper.toTaskResponseFromModel(taskRepository.save(task));
    }

    public Page<TaskResponse> getTasks(TaskFilter filter, Pageable pageable) {

        Specification<Task> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (filter.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }

        if (filter.getDueDateFrom() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
        }

        if (filter.getDueDateTo() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
        }

        if (filter.getAssignedUserId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.join("assignedUser").get("id"), filter.getAssignedUserId()));
        }

        Page<Task> page = taskRepository.findAll(spec, pageable);

        return page.map(taskMapper::toTaskResponseFromModel);
    }
}
