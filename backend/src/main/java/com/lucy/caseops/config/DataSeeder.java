package com.lucy.caseops.config;

import com.lucy.caseops.user.Role;
import com.lucy.caseops.user.User;
import com.lucy.caseops.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("partner@example.com").isEmpty()) {
                userRepository.save(new User(
                        "Demo Partner",
                        "partner@example.com",
                        passwordEncoder.encode("password123"),
                        Role.PARTNER
                ));
            }

            if (userRepository.findByEmail("paralegal@example.com").isEmpty()) {
                userRepository.save(new User(
                        "Demo Paralegal",
                        "paralegal@example.com",
                        passwordEncoder.encode("password123"),
                        Role.PARALEGAL
                ));
            }

            if (userRepository.findByEmail("lawyer@example.com").isEmpty()) {
                userRepository.save(new User(
                        "Demo Lawyer",
                        "lawyer@example.com",
                        passwordEncoder.encode("password123"),
                        Role.LAWYER
                ));
            }

            if (userRepository.findByEmail("client@example.com").isEmpty()) {
                userRepository.save(new User(
                        "Demo Client",
                        "client@example.com",
                        passwordEncoder.encode("password123"),
                        Role.CLIENT
                ));
            }
        };
    }
}
