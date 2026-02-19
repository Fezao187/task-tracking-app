package com.katlego.task_tracking_api.mapper;

import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.dto.user.AdminCreateUserRequest;
import com.katlego.task_tracking_api.dto.user.AdminCreateUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUserFromCreateUserRequest(AdminCreateUserRequest request);
    AdminCreateUserResponse toCreateUserResponseFromModel(User user);
}