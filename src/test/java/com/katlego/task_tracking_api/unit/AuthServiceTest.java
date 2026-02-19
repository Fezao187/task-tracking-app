package com.katlego.task_tracking_api.unit;

import com.katlego.task_tracking_api.domain.Role;
import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.dto.auth.AuthResponse;
import com.katlego.task_tracking_api.dto.auth.LoginRequest;
import com.katlego.task_tracking_api.dto.auth.RefreshTokenRequest;
import com.katlego.task_tracking_api.dto.auth.SignupRequest;
import com.katlego.task_tracking_api.exception.ResourceAlreadyExistException;
import com.katlego.task_tracking_api.exception.ResourceNotFoundException;
import com.katlego.task_tracking_api.mapper.AuthenticationMapper;
import com.katlego.task_tracking_api.repository.RoleRepository;
import com.katlego.task_tracking_api.repository.UserRepository;
import com.katlego.task_tracking_api.security.entity.CustomUserDetails;
import com.katlego.task_tracking_api.security.jwt.service.JwtService;
import com.katlego.task_tracking_api.security.service.RefreshTokenService;
import com.katlego.task_tracking_api.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuthenticationMapper authenticationMapper;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Role userRole;
    private User user;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName("USER");

        user = new User();
        user.setEmail("john@example.com");
        user.setUsername("johndoe");
        user.setRole(userRole);
        user.setPasswordHash("hashed_password");
    }

    @Test
    @DisplayName("Successful signup")
    void signup_success() {
        SignupRequest request = new SignupRequest("johndoe", "john@example.com", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(authenticationMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_password");
        when(jwtService.generateAccessToken(eq(user.getEmail()), anyMap())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("refresh_token");

        AuthResponse response = authService.signup(request);

        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        verify(userRepository).save(user);
        verify(refreshTokenService).saveRefreshToken(user.getEmail(), "refresh_token");
    }

    @Test
    @DisplayName("Throws exception email already exists")
    void signup_throwsWhenEmailAlreadyExists() {
        SignupRequest request = new SignupRequest("johndoe", "john@example.com", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ResourceAlreadyExistException.class)
                .hasMessageContaining(request.getEmail());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Throws exception username already exists")
    void signup_throwsWhenUsernameAlreadyExists() {
        SignupRequest request = new SignupRequest("johndoe", "john@example.com", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(ResourceAlreadyExistException.class)
                .hasMessageContaining(request.getUsername());

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Throws exception when default role not configured")
    void signup_throwsWhenDefaultRoleNotConfigured() {
        SignupRequest request = new SignupRequest("johndoe", "john@example.com", "password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Default role not configured");
    }

    @Test
    @DisplayName("Encode password before saving")
    void signup_encodesPasswordBeforeSaving() {
        SignupRequest request = new SignupRequest("johndoe", "john@example.com", "plaintext");

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(authenticationMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode("plaintext")).thenReturn("encoded_plaintext");
        when(jwtService.generateAccessToken(any(), anyMap())).thenReturn("at");
        when(jwtService.generateRefreshToken(any())).thenReturn("rt");

        authService.signup(request);

        verify(passwordEncoder).encode("plaintext");
        assertThat(user.getPasswordHash()).isEqualTo("encoded_plaintext");
    }

    @Test
    @DisplayName("Successfully logged in")
    void login_success() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(eq(user.getEmail()), anyMap())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("refresh_token");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        verify(refreshTokenService).saveRefreshToken(user.getEmail(), "refresh_token");
    }

    @Test
    @DisplayName("Throws exception when credentials are invalid")
    void login_throwsWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("john@example.com", "wrong_password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Throws exception when authenticated user not found in the db")
    void login_throwsWhenAuthenticatedUserNotFoundInDb() {
        LoginRequest request = new LoginRequest("ghost@example.com", "password");
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("ghost@example.com");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authenticated user not found in database");
    }

    @Test
    @DisplayName("Pass correct credentials to authentication manager")
    void login_passesCorrectCredentialsToAuthenticationManager() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), anyMap())).thenReturn("at");
        when(jwtService.generateRefreshToken(any())).thenReturn("rt");

        authService.login(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("john@example.com", "password123")
        );
    }


    @Test
    @DisplayName("Successfully generating refresh token")
    void refreshToken_success() {
        RefreshTokenRequest request = new RefreshTokenRequest("old_refresh_token");
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(jwtService.extractUsername("old_refresh_token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.validateRefreshToken(eq("old_refresh_token"), any())).thenReturn(true);
        when(refreshTokenService.isRefreshTokenValid(user.getEmail(), "old_refresh_token")).thenReturn(true);
        when(jwtService.generateAccessToken(eq(user.getEmail()), anyMap())).thenReturn("new_access_token");
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("new_refresh_token");

        AuthResponse response = authService.refreshToken(request);

        assertThat(response.getAccessToken()).isEqualTo("new_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("new_refresh_token");
        verify(refreshTokenService).rotateRefreshToken(user.getEmail(), "old_refresh_token", "new_refresh_token");
    }

    @Test
    @DisplayName("Throws exception when token user not found")
    void refreshToken_throwsWhenUserNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest("some_token");

        when(jwtService.extractUsername("some_token")).thenReturn("ghost@example.com");
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Throws exception when token invalid")
    void refreshToken_throwsWhenJwtValidationFails() {
        RefreshTokenRequest request = new RefreshTokenRequest("tampered_token");

        when(jwtService.extractUsername("tampered_token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.validateRefreshToken(eq("tampered_token"), any())).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    @DisplayName("Throws exception when token not recognized")
    void refreshToken_throwsWhenTokenNotRecognizedInStore() {
        RefreshTokenRequest request = new RefreshTokenRequest("unrecognized_token");

        when(jwtService.extractUsername("unrecognized_token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.validateRefreshToken(eq("unrecognized_token"), any())).thenReturn(true);
        when(refreshTokenService.isRefreshTokenValid(user.getEmail(), "unrecognized_token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Refresh token not recognized");
    }

    @Test
    @DisplayName("Rotate refresh toke rather than saving new")
    void refreshToken_rotatesTokenRatherThanSavingNew() {
        RefreshTokenRequest request = new RefreshTokenRequest("old_token");

        when(jwtService.extractUsername("old_token")).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.validateRefreshToken(eq("old_token"), any())).thenReturn(true);
        when(refreshTokenService.isRefreshTokenValid(user.getEmail(), "old_token")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), anyMap())).thenReturn("at");
        when(jwtService.generateRefreshToken(any())).thenReturn("new_token");

        authService.refreshToken(request);

        verify(refreshTokenService, never()).saveRefreshToken(any(), any());
        verify(refreshTokenService).rotateRefreshToken(user.getEmail(), "old_token", "new_token");
    }
}