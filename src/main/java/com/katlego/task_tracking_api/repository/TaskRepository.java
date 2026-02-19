package com.katlego.task_tracking_api.repository;

import com.katlego.task_tracking_api.domain.Task;
import com.katlego.task_tracking_api.domain.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
   List<Task> findTaskByAssignedUser(Long assignedUserId);
   List<Task> findByDueDateBeforeAndStatusIn(Instant now, List<TaskStatus> statuses);

   @Modifying
   @Query("""
        UPDATE Task t
        SET t.status = :overdueStatus
        WHERE t.dueDate < :now
        AND t.status IN :statuses
    """)
   int markTasksAsOverdue(
           @Param("now") Instant now,
           @Param("statuses") List<TaskStatus> statuses,
           @Param("overdueStatus") TaskStatus overdueStatus
   );
}