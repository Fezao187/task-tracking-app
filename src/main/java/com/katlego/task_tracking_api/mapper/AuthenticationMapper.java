package com.katlego.task_tracking_api.mapper;

import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.dto.auth.SignupRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AuthenticationMapper {
    User toUser(SignupRequest request);
}
