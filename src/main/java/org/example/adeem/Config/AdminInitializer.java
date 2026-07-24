package org.example.adeem.Config;

import lombok.RequiredArgsConstructor;
import org.example.adeem.Enums.Role;
import org.example.adeem.Model.User;
import org.example.adeem.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "admin@adeem.sa";
    private static final String ADMIN_PASSWORD = "Admin@12345";

    @PostConstruct
    public void initAdmin() {

        if (userRepository.existsByEmail(ADMIN_EMAIL)) {
            return;
        }

        User admin = new User();
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setFullName("System Admin");
        admin.setPhoneNumber("0500000000");
        admin.setRole(Role.ADMIN);

        userRepository.save(admin);

        System.out.println("-- Admin account created --");
        System.out.println("Email: " + ADMIN_EMAIL);
        System.out.println("Password: " + ADMIN_PASSWORD);
        System.out.println("--------------------------");
    }
}