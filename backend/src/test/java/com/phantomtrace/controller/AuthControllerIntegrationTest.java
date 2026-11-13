package com.phantomtrace.controller;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Full-stack integration test for the auth flow + JWT security enforcement.
 * Runs against H2 (test profile) with each test rolled back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    private static final String AUTH = "/api/v1/auth";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_returnsCreatedWithTokenAndAnalystRole() throws Exception {
        Map<String, Object> body = Map.of(
                "email", "analyst@phantomtrace.local",
                "fullName", "Test Analyst",
                "password", "password123");

        mockMvc.perform(post(AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("analyst@phantomtrace.local"))
                .andExpect(jsonPath("$.data.user.role").value("ROLE_ANALYST"));
    }

    @Test
    void register_duplicateEmailReturnsConflict() throws Exception {
        register("dup@example.com", "Dup User", "password123");

        Map<String, Object> duplicate = Map.of(
                "email", "dup@example.com",
                "fullName", "Another User",
                "password", "password123");

        mockMvc.perform(post(AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_shortPasswordReturnsBadRequest() throws Exception {
        Map<String, Object> body = Map.of(
                "email", "short@example.com",
                "fullName", "Short Pw",
                "password", "short");

        mockMvc.perform(post(AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_validCredentialsReturnsToken() throws Exception {
        register("login@example.com", "Login User", "password123");

        Map<String, Object> body = Map.of("email", "login@example.com", "password", "password123");

        mockMvc.perform(post(AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("login@example.com"));
    }

    @Test
    void login_wrongPasswordReturnsUnauthorizedWithGenericMessage() throws Exception {
        register("wrongpw@example.com", "Wrong Pw", "password123");

        Map<String, Object> body = Map.of("email", "wrongpw@example.com", "password", "wrong-password");

        mockMvc.perform(post(AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void login_unknownEmailReturnsUnauthorizedWithSameGenericMessage() throws Exception {
        Map<String, Object> body = Map.of("email", "ghost@example.com", "password", "password123");

        mockMvc.perform(post(AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void meWithValidTokenReturnsCurrentUser() throws Exception {
        String token = register("me@example.com", "Me User", "password123");

        mockMvc.perform(get(AUTH + "/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("me@example.com"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ANALYST"));
    }

    @Test
    void meWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get(AUTH + "/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedPathWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/investigations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthRemainsPublicWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk());
    }

    // --- helpers ---

    /** Registers a user and returns the issued JWT token. */
    private String register(String email, String fullName, String password) throws Exception {
        Map<String, Object> body = Map.of("email", email, "fullName", fullName, "password", password);
        String response = mockMvc.perform(post(AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response);
        return root.at("/data/token").asText();
    }
}
