package com.katlego.task_tracking_api.controller;

import com.katlego.task_tracking_api.docs.ApiDocs;
import com.katlego.task_tracking_api.dto.auth.AuthResponse;
import com.katlego.task_tracking_api.dto.auth.LoginRequest;
import com.katlego.task_tracking_api.dto.auth.RefreshTokenRequest;
import com.katlego.task_tracking_api.dto.auth.SignupRequest;
import com.katlego.task_tracking_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = ApiDocs.AuthApi.TAG)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(
            summary = ApiDocs.AuthApi.SIGNUP_SUMMARY,
            description = ApiDocs.AuthApi.SIGNUP_DESC
    )
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return new ResponseEntity<>(authService.signup(request), HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = ApiDocs.AuthApi.REFRESH_SUMMARY,
            description = ApiDocs.AuthApi.REFRESH_DESC
    )
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return new ResponseEntity<>(authService.refreshToken(request), HttpStatus.OK);
    }

    @PostMapping("/login")
    @Operation(
            summary = ApiDocs.AuthApi.LOGIN_SUMMARY,
            description = ApiDocs.AuthApi.LOGIN_DESC
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }
}
