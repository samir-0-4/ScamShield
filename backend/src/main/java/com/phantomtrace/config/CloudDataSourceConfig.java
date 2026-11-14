package com.phantomtrace.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;

/**
 * Production DataSource configuration designed for cloud platforms like Render, Railway, and Heroku.
 * Automatically parses raw DATABASE_URL (postgresql://user:pass@host:port/dbname or postgres://...)
 * into a proper JDBC URL (jdbc:postgresql://...) with extracted credentials, preventing the
 * "URL must start with 'jdbc'" failure.
 */
@Configuration
@Profile("prod")
public class CloudDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudDataSourceConfig.class);

    @Value("${DATABASE_URL:}")
    private String databaseUrl;

    @Value("${POSTGRES_USER:}")
    private String postgresUser;

    @Value("${POSTGRES_PASSWORD:}")
    private String postgresPassword;

    public static class ParsedDatabaseConfig {
        public final String jdbcUrl;
        public final String username;
        public final String password;

        public ParsedDatabaseConfig(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }
    }

    public static ParsedDatabaseConfig parseUrl(String rawUrl, String defaultUser, String defaultPassword) {
        String cleanUrl = rawUrl != null ? rawUrl.trim() : "";
        String jdbcUrl;
        String username = defaultUser;
        String password = defaultPassword;

        if (cleanUrl.startsWith("postgres://") || cleanUrl.startsWith("postgresql://")) {
            try {
                String uriString = cleanUrl.startsWith("postgres://")
                        ? "postgresql://" + cleanUrl.substring("postgres://".length())
                        : cleanUrl;
                URI uri = URI.create(uriString);

                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }

                String query = uri.getQuery();
                StringBuilder sb = new StringBuilder("jdbc:postgresql://")
                        .append(host)
                        .append(":")
                        .append(port)
                        .append("/")
                        .append(path != null ? path : "");

                if (query != null && !query.isBlank()) {
                    sb.append("?").append(query);
                }
                jdbcUrl = sb.toString();

                if (uri.getUserInfo() != null && !uri.getUserInfo().isBlank()) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    username = userParts[0];
                    if (userParts.length > 1) {
                        password = userParts[1];
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse DATABASE_URL as URI: {}, prepending jdbc:", e.getMessage());
                jdbcUrl = cleanUrl.startsWith("jdbc:") ? cleanUrl : "jdbc:" + cleanUrl;
            }
        } else if (cleanUrl.startsWith("jdbc:")) {
            jdbcUrl = cleanUrl;
        } else if (!cleanUrl.isBlank()) {
            jdbcUrl = "jdbc:postgresql://" + cleanUrl;
        } else {
            jdbcUrl = "jdbc:postgresql://localhost:5432/phantomtrace";
        }

        return new ParsedDatabaseConfig(jdbcUrl, username, password);
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("Initializing CloudDataSourceConfig for production profile...");
        ParsedDatabaseConfig parsed = parseUrl(databaseUrl, postgresUser, postgresPassword);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(parsed.jdbcUrl);
        config.setDriverClassName("org.postgresql.Driver");

        if (parsed.username != null && !parsed.username.isBlank()) {
            config.setUsername(parsed.username);
        }
        if (parsed.password != null && !parsed.password.isBlank()) {
            config.setPassword(parsed.password);
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(20000);
        // Do not fail startup immediately if the database is still initializing
        config.setInitializationFailTimeout(-1);
        config.setPoolName("PhantomTraceHikariPool");

        log.info("CloudDataSourceConfig created pool for target: {} (user: {})", parsed.jdbcUrl,
                parsed.username != null ? parsed.username : "(none)");
        return new HikariDataSource(config);
    }
}
