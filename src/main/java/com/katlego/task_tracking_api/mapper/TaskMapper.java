package com.katlego.task_tracking_api.mapper;

import com.katlego.task_tracking_api.domain.Task;
import com.katlego.task_tracking_api.dto.task.TaskRequest;
import com.katlego.task_tracking_api.dto.task.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    Task toTaskFromRequest(TaskRequest request);
    @Mapping(target = "assignedUserId", source = "model.assignedUser.id")
    @Mapping(target = "assignedUsername", source = "model.assignedUser.username")
    TaskResponse toTaskResponseFromModel(Task model);
}
