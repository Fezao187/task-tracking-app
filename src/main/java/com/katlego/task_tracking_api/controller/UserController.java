package com.katlego.task_tracking_api.controller;

import com.katlego.task_tracking_api.docs.ApiDocs;
import com.katlego.task_tracking_api.dto.user.AdminCreateUserRequest;
import com.katlego.task_tracking_api.dto.user.AdminCreateUserResponse;
import com.katlego.task_tracking_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = ApiDocs.UserApi.TAG)
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    @Operation(
            summary = ApiDocs.UserApi.ADMIN_CREATE_USER_SUMMARY,
            description = ApiDocs.UserApi.ADMIN_CREATE_USER_DESC
    )
    public ResponseEntity<AdminCreateUserResponse> adminCreateUser(@RequestBody AdminCreateUserRequest request){
        return new ResponseEntity<>(userService.adminCreateUser(request), HttpStatus.CREATED);
    }
}
