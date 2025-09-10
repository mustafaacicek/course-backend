package com.course.app.config;

import com.course.app.entity.Role;
import com.course.app.entity.User;
import com.course.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.superadmin.username}")
    private String superadminUsername;

    @Value("${app.default.superadmin.password}")
    private String superadminPassword;

    @Value("${app.default.admin.username}")
    private String adminUsername;

    @Value("${app.default.admin.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Create superadmin if not exists
        if (!userRepository.existsByUsername(superadminUsername)) {
            User superadmin = new User();
            superadmin.setUsername(superadminUsername);
            superadmin.setPassword(passwordEncoder.encode(superadminPassword));
            superadmin.setRole(Role.SUPERADMIN);
            userRepository.save(superadmin);
            System.out.println("Superadmin user created: " + superadminUsername);
        }

        // Create admin if not exists
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            System.out.println("Admin user created: " + adminUsername);
        }
    }
}
