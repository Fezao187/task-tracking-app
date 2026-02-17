package com.katlego.task_tracking_api.seeder;

import com.katlego.task_tracking_api.domain.Role;
import com.katlego.task_tracking_api.domain.User;
import com.katlego.task_tracking_api.exception.ResourceNotFoundException;
import com.katlego.task_tracking_api.repository.RoleRepository;
import com.katlego.task_tracking_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminSeeder implements CommandLineRunner {
    private final String username;
    private final String email;
    private final String password;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(
            @Value("${app.admin.username}") String username,
            @Value("${app.admin.email}") String email,
            @Value("${app.admin.password}") String password,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Seeding admin user......");
        if (userRepository.findByEmail(email).isPresent()) {
            log.error("Admin user already seeded, no need to seed.");
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(()-> new ResourceNotFoundException("Role not found!"));

        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(adminRole);

        userRepository.save(admin);
        log.info("Successfully seeded admin user!");
    }
}