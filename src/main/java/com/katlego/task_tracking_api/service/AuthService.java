package com.katlego.task_tracking_api.service;

import com.katlego.task_tracking_api.domain.Role;
import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.dto.auth.AuthResponse;
import com.katlego.task_tracking_api.dto.auth.LoginRequest;
import com.katlego.task_tracking_api.dto.auth.SignupRequest;
import com.katlego.task_tracking_api.exception.ResourceAlreadyExistException;
import com.katlego.task_tracking_api.mapper.AuthenticationMapper;
import com.katlego.task_tracking_api.repository.RoleRepository;
import com.katlego.task_tracking_api.repository.UserRepository;
import com.katlego.task_tracking_api.security.entity.CustomUserDetails;
import com.katlego.task_tracking_api.security.jwt.service.JwtService;
import com.katlego.task_tracking_api.security.service.RefreshTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService, AuthenticationMapper authenticationMapper, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationMapper = authenticationMapper;
        this.authenticationManager = authenticationManager;
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

        return generateTokens(user);
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in database"
                ));

        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        if (user.getRole() == null) {
            throw new IllegalStateException("User " + user.getEmail() + " has no role assigned");
        }

        Map<String, Object> claims = Map.of(
                "username", user.getUsername(),
                "role", user.getRole().getName()
        );

        String accessToken = jwtService.generateAccessToken(user.getEmail(), claims);

        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken);

        return new AuthResponse(accessToken, refreshToken);
    }
}