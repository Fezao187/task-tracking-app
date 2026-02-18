package com.katlego.task_tracking_api.controller;

import com.katlego.task_tracking_api.dto.task.TaskRequest;
import com.katlego.task_tracking_api.dto.task.TaskResponse;
import com.katlego.task_tracking_api.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id){
        return new ResponseEntity<>(taskService.getTaskById(id), HttpStatus.OK);
    }
    //Todo: Add endpoint for getting task of assigned user
    //Todo: Add endpoint for getting all tasks
}