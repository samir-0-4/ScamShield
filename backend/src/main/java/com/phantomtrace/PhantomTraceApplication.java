package com.phantomtrace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@SpringBootApplication
public class PhantomTraceApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(PhantomTraceApplication.class, args);
    }

    private static void loadDotenv() {
        List<Path> candidates = List.of(
                Paths.get(".env"),
                Paths.get("backend/.env"),
                Paths.get("../.env"),
                Paths.get("../backend/.env")
        );

        for (Path envPath : candidates) {
            if (Files.isRegularFile(envPath)) {
                try {
                    List<String> lines = Files.readAllLines(envPath, StandardCharsets.UTF_8);
                    for (String line : lines) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                            continue;
                        }
                        int eq = line.indexOf('=');
                        String key = line.substring(0, eq).trim();
                        String val = line.substring(eq + 1).trim();
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1);
                        }
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, val);
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }
}


