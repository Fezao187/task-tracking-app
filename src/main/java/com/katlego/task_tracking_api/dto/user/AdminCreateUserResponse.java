package com.katlego.task_tracking_api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateUserResponse {
    private String username;
    private String email;
}
