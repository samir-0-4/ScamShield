package com.phantomtrace.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CloudDataSourceConfigTest {

    @Test
    @DisplayName("Should convert postgresql URL to JDBC URL and extract credentials")
    void testPostgresqlConversion() {
        CloudDataSourceConfig.ParsedDatabaseConfig parsed = CloudDataSourceConfig.parseUrl(
                "postgresql://phantomtrace_user:secretpass@dpg-abc123-a.oregon-postgres.render.com:5432/phantomtrace",
                null, null);

        assertThat(parsed.jdbcUrl)
                .isEqualTo("jdbc:postgresql://dpg-abc123-a.oregon-postgres.render.com:5432/phantomtrace");
        assertThat(parsed.username).isEqualTo("phantomtrace_user");
        assertThat(parsed.password).isEqualTo("secretpass");
    }

    @Test
    @DisplayName("Should convert postgres scheme URL to JDBC URL and extract credentials")
    void testPostgresSchemeConversion() {
        CloudDataSourceConfig.ParsedDatabaseConfig parsed = CloudDataSourceConfig.parseUrl(
                "postgres://myuser:mypass@renderhost.com:5432/mydb",
                null, null);

        assertThat(parsed.jdbcUrl).isEqualTo("jdbc:postgresql://renderhost.com:5432/mydb");
        assertThat(parsed.username).isEqualTo("myuser");
        assertThat(parsed.password).isEqualTo("mypass");
    }

    @Test
    @DisplayName("Should preserve existing JDBC URL and fallback credentials")
    void testJdbcUrlPreserved() {
        CloudDataSourceConfig.ParsedDatabaseConfig parsed = CloudDataSourceConfig.parseUrl(
                "jdbc:postgresql://localhost:5432/phantomtrace",
                "local_user", "local_pass");

        assertThat(parsed.jdbcUrl).isEqualTo("jdbc:postgresql://localhost:5432/phantomtrace");
        assertThat(parsed.username).isEqualTo("local_user");
        assertThat(parsed.password).isEqualTo("local_pass");
    }
}
