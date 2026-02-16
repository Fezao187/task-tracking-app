package com.katlego.task_tracking_api.service;

import com.katlego.task_tracking_api.domain.Role;
import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.dto.auth.AuthResponse;
import com.katlego.task_tracking_api.dto.auth.SignupRequest;
import com.katlego.task_tracking_api.exception.ResourceAlreadyExistException;
import com.katlego.task_tracking_api.mapper.AuthenticationMapper;
import com.katlego.task_tracking_api.repository.RoleRepository;
import com.katlego.task_tracking_api.repository.UserRepository;
import com.katlego.task_tracking_api.security.jwt.service.JwtService;
import com.katlego.task_tracking_api.security.service.RefreshTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationMapper authenticationMapper;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationMapper authenticationMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationMapper = authenticationMapper;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistException("Email already exists: "+request.getEmail());
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResourceAlreadyExistException("Username already exists: "+request.getUsername());
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role not configured"));

        User user = authenticationMapper.toUser(request);
        user.setRole(userRole);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                Map.of("username", user.getUsername(),
                        "role", user.getRole().getName())
        );

        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        return new AuthResponse(accessToken, refreshToken);
    }
}