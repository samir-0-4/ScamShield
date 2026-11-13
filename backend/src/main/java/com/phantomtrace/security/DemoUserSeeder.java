package com.phantomtrace.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.phantomtrace.entity.User;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.repository.UserRepository;

/**
 * Seeds a single demo {@code ROLE_ANALYST} account for hackathon demonstration.
 * Only active under the {@code dev} profile AND when
 * {@code phantomtrace.demo.seed-enabled=true} (default off), and only when no
 * users exist yet. Never runs in tests (the property defaults to false).
 */
@Component
@Profile("dev")
@ConditionalOnProperty(value = "phantomtrace.demo.seed-enabled", havingValue = "true")
public class DemoUserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);
    private static final String DEMO_EMAIL = "analyst@phantomtrace.local";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seededPassword;

    public DemoUserSeeder(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${phantomtrace.demo.seed-password:PhantomTrace2026!}") String seededPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.seededPassword = seededPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.debug("Demo seed skipped: users already present.");
            return;
        }
        // Credentials come from configuration, never hardcoded in logic.
        User user = new User(
                DEMO_EMAIL,
                passwordEncoder.encode(seededPassword),
                "PhantomTrace Analyst",
                UserRole.ROLE_ANALYST
        );
        userRepository.save(user);
        log.info("Demo analyst account seeded: {}", DEMO_EMAIL);
    }
}
