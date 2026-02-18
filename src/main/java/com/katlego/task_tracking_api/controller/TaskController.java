package com.katlego.task_tracking_api.controller;

import com.katlego.task_tracking_api.domain.TaskStatus;
import com.katlego.task_tracking_api.dto.task.TaskAssignRequest;
import com.katlego.task_tracking_api.dto.task.TaskDeleteResponse;
import com.katlego.task_tracking_api.dto.task.TaskFilter;
import com.katlego.task_tracking_api.dto.task.TaskRequest;
import com.katlego.task_tracking_api.dto.task.TaskResponse;
import com.katlego.task_tracking_api.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request){
        return new ResponseEntity<>(taskService.createTask(request), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update")
    public ResponseEntity<TaskResponse> updateTask(@RequestBody TaskRequest request, @RequestParam Long taskId){
        return new ResponseEntity<>(taskService.updateTask(taskId, request), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponse>> getTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueDateTo,
            @RequestParam(required = false) Long assignedUserId,
            Pageable pageable
    ){
        TaskFilter filter = new TaskFilter();
        filter.setStatus(status);
        filter.setDueDateFrom(dueDateFrom);
        filter.setDueDateTo(dueDateTo);
        filter.setAssignedUserId(assignedUserId);

        return new ResponseEntity<>(taskService.getTasks(filter, pageable), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/assign")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable Long id, @RequestBody TaskAssignRequest request){
        return new ResponseEntity<>(taskService.assignTask(id, request), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id){
        return new ResponseEntity<>(taskService.getTaskById(id), HttpStatus.OK);
    }

    @GetMapping("/my-tasks")
    public ResponseEntity<List<TaskResponse>> getAllMyAssignedTasks(){
        return new ResponseEntity<>(taskService.getAllMyAssignedTasks(),HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<TaskResponse>> getAllTasks(){
        return new ResponseEntity<>(taskService.getAllTasks(),HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<TaskDeleteResponse> deleteTask(@PathVariable Long id){
        return new ResponseEntity<>(taskService.deleteTaskById(id), HttpStatus.OK);
    }
}
