package com.katlego.task_tracking_api.security.service;

import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.exception.ResourceNotFoundException;
import com.katlego.task_tracking_api.repository.UserRepository;
import com.katlego.task_tracking_api.security.entity.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRole(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email: " + email + " not found!"));
        return new CustomUserDetails(user);
    }
}
