package com.phantomtrace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Normalizes cloud provider database URLs (e.g. Render, Railway, Heroku) where DATABASE_URL
 * is provided in the format postgresql://user:pass@host:port/dbname or postgres://...
 * into standard JDBC format required by Spring Boot and Flyway:
 * jdbc:postgresql://host:port/dbname along with spring.datasource.username and password.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DatabaseUrlEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawDbUrl = environment.getProperty("DATABASE_URL");
        if (rawDbUrl == null || rawDbUrl.isBlank()) {
            return;
        }

        if (rawDbUrl.startsWith("postgres://") || rawDbUrl.startsWith("postgresql://")) {
            try {
                String normalizedUriString = rawDbUrl.startsWith("postgres://")
                        ? "postgresql://" + rawDbUrl.substring("postgres://".length())
                        : rawDbUrl;

                URI uri = URI.create(normalizedUriString);
                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }

                String query = uri.getQuery();
                StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                        .append(host)
                        .append(":")
                        .append(port)
                        .append("/")
                        .append(path != null ? path : "");

                if (query != null && !query.isBlank()) {
                    jdbcUrl.append("?").append(query);
                }

                Map<String, Object> additionalProps = new HashMap<>();
                additionalProps.put("spring.datasource.url", jdbcUrl.toString());

                String userInfo = uri.getUserInfo();
                if (userInfo != null && !userInfo.isBlank()) {
                    String[] userParts = userInfo.split(":", 2);
                    additionalProps.put("spring.datasource.username", userParts[0]);
                    if (userParts.length > 1) {
                        additionalProps.put("spring.datasource.password", userParts[1]);
                    }
                }

                log.info("Normalized Cloud DATABASE_URL to JDBC target: jdbc:postgresql://{}:{}/{}", host, port, path);
                environment.getPropertySources().addFirst(new MapPropertySource("cloudDatabaseUrlProperties", additionalProps));
            } catch (Exception e) {
                log.warn("Failed to parse cloud DATABASE_URL as URI, keeping original URL: {}", e.getMessage());
            }
        }
    }
}
