package org.project.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.project.model.Role;
import org.project.model.User;
import org.project.repository.RoleRepository;
import org.project.repository.UserRepository;
import org.project.repository.UserRoleJdbcRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${server.username-admin}")
    String ADMIN_USER_NAME;

    @NonFinal
    @Value("${server.password-admin}")
    String ADMIN_PASSWORD;

    @NonFinal
    private static final String ADMIN_EMAIL = "admin@gmail.com";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        UserRoleJdbcRepository userRoleJdbcRepository) {
        log.info("Initializing application.....");
        return args -> {
            Optional<Role> roleAdmin = roleRepository.findByName("ADMIN");

            if (userRepository.findByUsername(ADMIN_USER_NAME).isEmpty()) {
                User user = User.builder()
                        .username(ADMIN_USER_NAME)
                        .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                        .email(ADMIN_EMAIL)
                        .isActive(true)
                        .isEmailVerified(true)
                        .build();
                userRepository.save(user);

                log.info("Admin user created with username: {}", ADMIN_USER_NAME);
                userRoleJdbcRepository.assignRoleToUser(user.getId(), roleAdmin.get().getId(),null,null);
            } else {
                log.info("Admin user already exists.");
            }
        };
    }

}
