package com.katlego.task_tracking_api.common;

import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.exception.ResourceNotFoundException;
import com.katlego.task_tracking_api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserComponent {
    private final UserRepository userRepository;

    public AuthenticatedUserComponent(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentLoggedInUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationException("No authenticated user") {
            };
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User with email " + email + " not found"));
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getCurrentLoggedInUser().getRole().getName());
    }
}
