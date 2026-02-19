package com.katlego.task_tracking_api.unit;

import com.katlego.task_tracking_api.common.AuthenticatedUserComponent;
import com.katlego.task_tracking_api.domain.Task;
import com.katlego.task_tracking_api.domain.TaskStatus;
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
import com.katlego.task_tracking_api.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private AuthenticatedUserComponent authenticatedUserComponent;
    @Mock private TaskMapper taskMapper;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskResponse taskResponse;
    private TaskRequest taskRequest;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");

        task = new Task();
        task.setId(10L);
        task.setTitle("Test task");
        task.setDescription("Description");
        task.setStatus(TaskStatus.NEW);
        task.setDueDate(Instant.now().plusSeconds(3600));
        task.setAssignedUser(user);

        taskResponse = new TaskResponse();
        taskResponse.setId(task.getId());
        taskResponse.setTitle(task.getTitle());

        taskRequest = new TaskRequest();
        taskRequest.setTitle("New title");
        taskRequest.setDescription("New description");
        taskRequest.setStatus(TaskStatus.IN_PROGRESS);
        taskRequest.setDueDate(Instant.now().plusSeconds(7200));
    }

    @Test
    @DisplayName("createTask saves task and returns response")
    void createTask_success() {
        Task mapped = new Task();

        when(taskMapper.toTaskFromRequest(taskRequest)).thenReturn(mapped);
        when(taskRepository.save(mapped)).thenReturn(task);
        when(taskMapper.toTaskResponseFromModel(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.createTask(taskRequest);

        assertThat(result).isEqualTo(taskResponse);
        verify(taskRepository).save(mapped);
        verify(taskMapper).toTaskFromRequest(taskRequest);
        verify(taskMapper).toTaskResponseFromModel(task);
    }

    @Test
    @DisplayName("updateTask updates existing task")
    void updateTask_success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toTaskResponseFromModel(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.updateTask(10L, taskRequest);

        assertThat(result).isEqualTo(taskResponse);
        assertThat(task.getTitle()).isEqualTo(taskRequest.getTitle());
        assertThat(task.getStatus()).isEqualTo(taskRequest.getStatus());
        assertThat(task.getDescription()).isEqualTo(taskRequest.getDescription());
        assertThat(task.getDueDate()).isEqualTo(taskRequest.getDueDate());
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("updateTask throws when task not found")
    void updateTask_notFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(99L, taskRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task with id: 99");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTaskById returns task response")
    void getTaskById_success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskMapper.toTaskResponseFromModel(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.getTaskById(10L);

        assertThat(result).isEqualTo(taskResponse);
    }

    @Test
    @DisplayName("getTaskById throws when task not found")
    void getTaskById_notFound() {
        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task with id: 10");
    }

    @Test
    @DisplayName("getAllMyAssignedTasks returns tasks for current user")
    void getAllMyAssignedTasks_success() {
        when(authenticatedUserComponent.getCurrentLoggedInUser()).thenReturn(user);
        when(taskRepository.findTaskByAssignedUser(user.getId())).thenReturn(List.of(task));
        when(taskMapper.toTaskResponseFromModel(task)).thenReturn(taskResponse);

        List<TaskResponse> result = taskService.getAllMyAssignedTasks();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(taskResponse);
        verify(taskRepository).findTaskByAssignedUser(user.getId());
    }

    @Test
    @DisplayName("getAllTasks returns all tasks")
    void getAllTasks_success() {
        when(taskRepository.findAll()).thenReturn(List.of(task));
        when(taskMapper.toTaskResponseFromModel(task)).thenReturn(taskResponse);

        List<TaskResponse> result = taskService.getAllTasks();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(taskResponse);
        verify(taskRepository).findAll();
    }

    @Test
    @DisplayName("deleteTaskById deletes task when found")
    void deleteTaskById_success() {
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        TaskDeleteResponse response = taskService.deleteTaskById(10L);

        assertThat(response.getMessage()).contains("successfully deleted");
        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("deleteTaskById throws when task not found")
    void deleteTaskById_notFound() {
        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTaskById(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task with id: 10");

        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("assignTask sets assigned user and returns response")
    void assignTask_success() {
        TaskAssignRequest assignRequest = new TaskAssignRequest();
        assignRequest.setAssignedUserId(2L);
        User assignee = new User();
        assignee.setId(2L);
        assignee.setUsername("assignee");

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toTaskResponseFromModel(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.assignTask(10L, assignRequest);

        assertThat(result).isEqualTo(taskResponse);
        assertThat(task.getAssignedUser()).isEqualTo(assignee);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("assignTask throws when task not found")
    void assignTask_taskNotFound() {
        TaskAssignRequest assignRequest = new TaskAssignRequest();
        assignRequest.setAssignedUserId(2L);

        when(taskRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignTask(10L, assignRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task with id: 10");

        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("assignTask throws when user not found")
    void assignTask_userNotFound() {
        TaskAssignRequest assignRequest = new TaskAssignRequest();
        assignRequest.setAssignedUserId(2L);

        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignTask(10L, assignRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User with id: 2");

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTasks delegates to repository and maps results")
    void getTasks_success() {
        TaskFilter filter = new TaskFilter();
        filter.setStatus(TaskStatus.NEW);
        filter.setAssignedUserId(1L);
        Pageable pageable = PageRequest.of(0, 10);

        Task anotherTask = new Task();
        anotherTask.setId(11L);
        TaskResponse anotherResponse = new TaskResponse();
        anotherResponse.setId(anotherTask.getId());

        List<Task> tasks = List.of(task, anotherTask);
        Page<Task> page = new PageImpl<>(tasks, pageable, tasks.size());

        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(taskMapper.toTaskResponseFromModel(task)).thenReturn(taskResponse);
        when(taskMapper.toTaskResponseFromModel(anotherTask)).thenReturn(anotherResponse);

        Page<TaskResponse> result = taskService.getTasks(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent())
                .extracting(TaskResponse::getId)
                .containsExactly(task.getId(), anotherTask.getId());

        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
    }
}
