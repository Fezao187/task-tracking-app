package com.katlego.task_tracking_api.service;

import com.katlego.task_tracking_api.domain.Role;
import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.dto.auth.AuthResponse;
import com.katlego.task_tracking_api.dto.auth.SignupRequest;
import com.katlego.task_tracking_api.dto.user.AdminCreateUserRequest;
import com.katlego.task_tracking_api.dto.user.AdminCreateUserResponse;
import com.katlego.task_tracking_api.exception.ResourceAlreadyExistException;
import com.katlego.task_tracking_api.exception.ResourceNotFoundException;
import com.katlego.task_tracking_api.mapper.AuthenticationMapper;
import com.katlego.task_tracking_api.mapper.UserMapper;
import com.katlego.task_tracking_api.repository.RoleRepository;
import com.katlego.task_tracking_api.repository.UserRepository;
import com.katlego.task_tracking_api.security.jwt.service.JwtService;
import com.katlego.task_tracking_api.security.service.RefreshTokenService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationMapper authenticationMapper, AuthenticationManager authenticationManager, AuthService authService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.userMapper = userMapper;
    }

    public AdminCreateUserResponse adminCreateUser(AdminCreateUserRequest request) {
        String loggedInUserEmail = authService.getLoggedInUserEmail();

        User loggedInUser = userRepository.findByEmail(loggedInUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + loggedInUserEmail + " does not exist."));
        if(loggedInUser.getRole().getName()!="ADMIN"){
            throw new AccessDeniedException("Only admins can create users");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistException("Email already exists: " + request.getEmail());
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResourceAlreadyExistException("Username already exists: " + request.getUsername());
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Default role not configured"));

        User user = userMapper.toUserFromCreateUserRequest(request);
        user.setRole(userRole);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return userMapper.toCreateUserResponseFromModel(userRepository.save(user));
    }
}
