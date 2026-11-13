package com.phantomtrace.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phantomtrace.enums.InvestigationCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EvidenceControllerIntegrationTest {

    private static final String AUTH = "/api/v1/auth";
    private static final String INVESTIGATIONS = "/api/v1/investigations";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createsListsAndDeletesMultipleEvidenceItems() throws Exception {
        String token = register("evidence-owner@example.com");
        String investigationId = createInvestigation(token);

        Map<String, Object> message = new HashMap<>();
        message.put("evidenceType", "TEXT_MESSAGE");
        message.put("textContent", "Urgent: pay the registration fee today at https://example.com/pay");
        String messageResponse = mockMvc.perform(post(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.evidenceType").value("TEXT_MESSAGE"))
                .andReturn().getResponse().getContentAsString();
        String messageEvidenceId = objectMapper.readTree(messageResponse).path("data").path("id").asText();
        mockMvc.perform(get("/api/v1/evidence/" + messageEvidenceId + "/indicators")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.indicatorType == 'URL')]").exists())
                .andExpect(jsonPath("$.data[?(@.indicatorType == 'DOMAIN')]").exists())
                .andExpect(jsonPath("$.data[?(@.indicatorType == 'PAYMENT_TERM')]").exists())
                .andExpect(jsonPath("$.data[?(@.indicatorType == 'MESSAGE_SIGNAL')]").exists());

        Map<String, Object> url = new HashMap<>();
        url.put("evidenceType", "URL");
        url.put("url", "https://example.com/login");
        String urlResponse = mockMvc.perform(post(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(url)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.evidenceType").value("URL"))
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> email = new HashMap<>();
        email.put("evidenceType", "EMAIL");
        email.put("senderAddress", "sender@example.com");
        email.put("subject", "Payment required");
        email.put("emailBody", "Please pay immediately.");
        mockMvc.perform(post(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.evidenceType").value("EMAIL"));

        mockMvc.perform(get(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));

        mockMvc.perform(delete("/api/v1/evidence/" + messageEvidenceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        String urlEvidenceId = objectMapper.readTree(urlResponse).path("data").path("id").asText();
        mockMvc.perform(get("/api/v1/evidence/" + urlEvidenceId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("https://example.com/login"));
    }

    @Test
    void preventsCrossUserEvidenceAccessAndRejectsInvalidUrl() throws Exception {
        String ownerToken = register("evidence-owner-two@example.com");
        String intruderToken = register("evidence-intruder@example.com");
        String investigationId = createInvestigation(ownerToken);

        Map<String, Object> invalid = new HashMap<>();
        invalid.put("evidenceType", "URL");
        invalid.put("url", "not-a-url");
        mockMvc.perform(post(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void acceptsPrivateImageUploadAndRejectsInvalidSignature() throws Exception {
        String token = register("evidence-file@example.com");
        String investigationId = createInvestigation(token);
        byte[] png = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");

        mockMvc.perform(multipart(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "metadata", "", MediaType.APPLICATION_JSON_VALUE,
                                "{\"evidenceType\":\"IMAGE\",\"description\":\"Screenshot\"}".getBytes()))
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "file", "screenshot.png", "image/png", png))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.evidenceType").value("IMAGE"))
                .andExpect(jsonPath("$.data.mimeType").value("image/png"));

        mockMvc.perform(multipart(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "metadata", "", MediaType.APPLICATION_JSON_VALUE,
                                "{\"evidenceType\":\"IMAGE\"}".getBytes()))
                        .file(new org.springframework.mock.web.MockMultipartFile(
                                "file", "fake.png", "image/png", "not-png".getBytes()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buildsInvestigationGraphFromRepeatedUrlAndDomainEvidence() throws Exception {
        String token = register("graph-owner@example.com");
        String investigationId = createInvestigation(token);
        String first = "{\"evidenceType\":\"TEXT_MESSAGE\",\"textContent\":\"Job offer: apply at https://jobs.example.test/apply\"}";
        String second = "{\"evidenceType\":\"TEXT_MESSAGE\",\"textContent\":\"The same recruiter sent https://jobs.example.test/next\"}";

        mockMvc.perform(post(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isCreated());
        mockMvc.perform(post(INVESTIGATIONS + "/" + investigationId + "/evidence")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(second))
                .andExpect(status().isCreated());

        mockMvc.perform(post(INVESTIGATIONS + "/" + investigationId + "/graph/generate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get(INVESTIGATIONS + "/" + investigationId + "/graph")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nodes.length()").value(7))
                .andExpect(jsonPath("$.data.edges[?(@.type == 'CONTAINS_URL')]").exists())
                .andExpect(jsonPath("$.data.edges[?(@.type == 'REFERENCES_DOMAIN')]").exists())
                .andExpect(jsonPath("$.data.edges[?(@.type == 'SHARES_INDICATOR')]").exists());
    }

    @Test
    void isolatesGraphAcrossInvestigations() throws Exception {
        String ownerToken = register("graph-isolation@example.com");
        String investigationId = createInvestigation(ownerToken);
        mockMvc.perform(get(INVESTIGATIONS + "/" + investigationId + "/graph")
                        .header("Authorization", "Bearer " + register("graph-other@example.com")))
                .andExpect(status().isForbidden());
    }

    private String register(String email) throws Exception {
        Map<String, Object> request = Map.of("email", email, "fullName", "Evidence Tester", "password", "Password123!");
        String response = mockMvc.perform(post(AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").path("token").asText();
    }

    private String createInvestigation(String token) throws Exception {
        Map<String, Object> request = Map.of(
                "title", "Evidence test case",
                "description", "Testing evidence isolation",
                "category", InvestigationCategory.PHISHING.name());
        String response = mockMvc.perform(post(INVESTIGATIONS)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(response).path("data");
        return data.path("id").asText();
    }
}
