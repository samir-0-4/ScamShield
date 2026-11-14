package com.phantomtrace.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.StandardEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUrlEnvironmentPostProcessorTest {

    private final DatabaseUrlEnvironmentPostProcessor processor = new DatabaseUrlEnvironmentPostProcessor();

    @Test
    @DisplayName("Should convert postgresql URI with user, pass, host, port, db to JDBC properties")
    void testConvertPostgresqlUrl() {
        ConfigurableEnvironment env = new StandardEnvironment();
        System.setProperty("DATABASE_URL",
                "postgresql://dbuser:supersecret@dpg-abc123-a.oregon-postgres.render.com:5432/phantomtrace_prod");

        try {
            processor.postProcessEnvironment(env, new SpringApplication());

            assertThat(env.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://dpg-abc123-a.oregon-postgres.render.com:5432/phantomtrace_prod");
            assertThat(env.getProperty("spring.datasource.username")).isEqualTo("dbuser");
            assertThat(env.getProperty("spring.datasource.password")).isEqualTo("supersecret");
        } finally {
            System.clearProperty("DATABASE_URL");
        }
    }

    @Test
    @DisplayName("Should convert postgres URI to JDBC properties")
    void testConvertPostgresUrl() {
        ConfigurableEnvironment env = new StandardEnvironment();
        System.setProperty("DATABASE_URL", "postgres://user1:pass1@host.render.com/db1");

        try {
            processor.postProcessEnvironment(env, new SpringApplication());

            assertThat(env.getProperty("spring.datasource.url"))
                    .isEqualTo("jdbc:postgresql://host.render.com:5432/db1");
            assertThat(env.getProperty("spring.datasource.username")).isEqualTo("user1");
            assertThat(env.getProperty("spring.datasource.password")).isEqualTo("pass1");
        } finally {
            System.clearProperty("DATABASE_URL");
        }
    }

    @Test
    @DisplayName("Should leave JDBC URLs untouched")
    void testLeaveJdbcUrlUntouched() {
        ConfigurableEnvironment env = new StandardEnvironment();
        System.setProperty("DATABASE_URL", "jdbc:postgresql://localhost:5432/testdb");

        try {
            processor.postProcessEnvironment(env, new SpringApplication());

            assertThat(env.getProperty("spring.datasource.url")).isNull();
        } finally {
            System.clearProperty("DATABASE_URL");
        }
    }
}
