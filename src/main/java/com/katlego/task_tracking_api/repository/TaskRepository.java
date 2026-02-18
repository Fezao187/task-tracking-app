package com.katlego.task_tracking_api.repository;

import com.katlego.task_tracking_api.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
   List<Task> findTaskByAssignedUser(Long assignedUserId);
}