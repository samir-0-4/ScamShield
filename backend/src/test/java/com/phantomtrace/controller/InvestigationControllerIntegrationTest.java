package com.phantomtrace.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.InvestigationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvestigationControllerIntegrationTest {

        private static final String BASE_URL = "/api/v1/investigations";
        private static final String AUTH_URL = "/api/v1/auth";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private String registerAndGetToken(String email, String fullName, String password) throws Exception {
                Map<String, Object> body = Map.of(
                                "email", email,
                                "fullName", fullName,
                                "password", password);
                String response = mockMvc.perform(post(AUTH_URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                JsonNode root = objectMapper.readTree(response);
                return root.path("data").path("token").asText();
        }

        private String createInvestigation(String token, String title, String description,
                        InvestigationCategory category) throws Exception {
                Map<String, Object> body = Map.of(
                                "title", title,
                                "description", description,
                                "category", category.name());
                String response = mockMvc.perform(post(BASE_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andReturn().getResponse().getContentAsString();

                JsonNode root = objectMapper.readTree(response);
                return root.path("data").path("id").asText();
        }

        @Test
        @DisplayName("unauthenticated requests return 401 Unauthorized")
        void unauthenticatedRequest_returns401() throws Exception {
                mockMvc.perform(get(BASE_URL))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("createInvestigation: creates investigation with valid JWT and fields")
        void createInvestigation_success() throws Exception {
                String token = registerAndGetToken("analyst1@example.com", "Analyst One", "Password123!");

                Map<String, Object> request = Map.of(
                                "title", "Cryptocurrency Investment WhatsApp Group",
                                "description", "Victim added to fraudulent trading signal group.",
                                "category", InvestigationCategory.INVESTMENT_SCAM.name());

                mockMvc.perform(post(BASE_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.id").isNotEmpty())
                                .andExpect(jsonPath("$.data.title").value("Cryptocurrency Investment WhatsApp Group"))
                                .andExpect(jsonPath("$.data.category").value("INVESTMENT_SCAM"))
                                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                                .andExpect(jsonPath("$.data.overallRiskLevel").value("UNKNOWN"));
        }

        @Test
        @DisplayName("createInvestigation: validation failure on empty title returns 400")
        void createInvestigation_validationFailure_blankTitle() throws Exception {
                String token = registerAndGetToken("analyst2@example.com", "Analyst Two", "Password123!");

                Map<String, Object> request = Map.of(
                                "title", "  ",
                                "category", InvestigationCategory.PHISHING.name());

                mockMvc.perform(post(BASE_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("listInvestigations: strictly isolates investigations across different users")
        void listInvestigations_multiTenantIsolation() throws Exception {
                String tokenA = registerAndGetToken("userA@example.com", "User A", "Password123!");
                String tokenB = registerAndGetToken("userB@example.com", "User B", "Password123!");

                createInvestigation(tokenA, "Case A-1", "Desc A", InvestigationCategory.JOB_SCAM);
                createInvestigation(tokenA, "Case A-2", "Desc A2", InvestigationCategory.PHISHING);
                createInvestigation(tokenB, "Case B-1", "Desc B", InvestigationCategory.PAYMENT_SCAM);

                // User A should only see their 2 cases
                mockMvc.perform(get(BASE_URL)
                                .header("Authorization", "Bearer " + tokenA))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.totalElements").value(2))
                                .andExpect(jsonPath("$.data.content[0].title").value("Case A-2"))
                                .andExpect(jsonPath("$.data.content[1].title").value("Case A-1"));

                // User B should only see their 1 case
                mockMvc.perform(get(BASE_URL)
                                .header("Authorization", "Bearer " + tokenB))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.totalElements").value(1))
                                .andExpect(jsonPath("$.data.content[0].title").value("Case B-1"));
        }

        @Test
        @DisplayName("getInvestigationById: owner retrieves 200, cross-user access returns 403")
        void getInvestigationById_crossUserProtection() throws Exception {
                String tokenOwner = registerAndGetToken("owner@example.com", "Owner", "Password123!");
                String tokenIntruder = registerAndGetToken("intruder@example.com", "Intruder", "Password123!");

                String invId = createInvestigation(tokenOwner, "Secret Threat Case", "Confidential",
                                InvestigationCategory.IDENTITY_FRAUD);

                // Owner can access
                mockMvc.perform(get(BASE_URL + "/" + invId)
                                .header("Authorization", "Bearer " + tokenOwner))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title").value("Secret Threat Case"));

                // Intruder gets 403 Forbidden
                mockMvc.perform(get(BASE_URL + "/" + invId)
                                .header("Authorization", "Bearer " + tokenIntruder))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.message").value(
                                                "Access denied: You do not have permission to access this investigation"));
        }

        @Test
        @DisplayName("patchInvestigation: owner updates fields, non-owner gets 403")
        void patchInvestigation_successAndProtection() throws Exception {
                String tokenOwner = registerAndGetToken("owner2@example.com", "Owner 2", "Password123!");
                String tokenIntruder = registerAndGetToken("intruder2@example.com", "Intruder 2", "Password123!");

                String invId = createInvestigation(tokenOwner, "Initial Title", "Initial Desc",
                                InvestigationCategory.OTHER);

                // Intruder cannot patch
                Map<String, Object> hackerPatch = Map.of("title", "Hacked Title");
                mockMvc.perform(patch(BASE_URL + "/" + invId)
                                .header("Authorization", "Bearer " + tokenIntruder)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(hackerPatch)))
                                .andExpect(status().isForbidden());

                // Owner can patch
                Map<String, Object> validPatch = Map.of(
                                "title", "Verified Fake Bank Phishing",
                                "status", InvestigationStatus.PROCESSING.name());
                mockMvc.perform(patch(BASE_URL + "/" + invId)
                                .header("Authorization", "Bearer " + tokenOwner)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validPatch)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.data.title").value("Verified Fake Bank Phishing"))
                                .andExpect(jsonPath("$.data.status").value("PROCESSING"));
        }

        @Test
        @DisplayName("deleteInvestigation: owner deletes case, cross-user deletion rejected with 403")
        void deleteInvestigation_successAndProtection() throws Exception {
                String tokenOwner = registerAndGetToken("owner3@example.com", "Owner 3", "Password123!");
                String tokenIntruder = registerAndGetToken("intruder3@example.com", "Intruder 3", "Password123!");

                String invId = createInvestigation(tokenOwner, "To Be Deleted", "Testing deletion",
                                InvestigationCategory.SHOPPING_SCAM);

                // Intruder deletion rejected
                mockMvc.perform(delete(BASE_URL + "/" + invId)
                                .header("Authorization", "Bearer " + tokenIntruder))
                                .andExpect(status().isForbidden());

                // Owner deletion succeeds
                mockMvc.perform(delete(BASE_URL + "/" + invId)
                                .header("Authorization", "Bearer " + tokenOwner))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                // Subsequent get returns 404
                mockMvc.perform(get(BASE_URL + "/" + invId)
                                .header("Authorization", "Bearer " + tokenOwner))
                                .andExpect(status().isNotFound());
        }
}
