package com.phantomtrace.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing for {@code @CreatedDate} / {@code @LastModifiedDate}
 * on entities such as {@link com.phantomtrace.entity.User}.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
