package com.katlego.task_tracking_api.mapper;

import com.katlego.task_tracking_api.domain.Task;
import com.katlego.task_tracking_api.dto.task.CreateTaskRequest;
import com.katlego.task_tracking_api.dto.task.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    Task toTaskFromRequest(CreateTaskRequest request);
    TaskResponse toTaskResponseFromModel(Task model);
}
