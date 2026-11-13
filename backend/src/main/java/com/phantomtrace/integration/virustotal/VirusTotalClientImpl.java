package com.phantomtrace.integration.virustotal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class VirusTotalClientImpl implements VirusTotalClient {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String apiKey;

    public VirusTotalClientImpl(ObjectMapper mapper,
            @Value("${phantomtrace.threat-intel.virustotal.base-url:https://www.virustotal.com/api/v3}") String baseUrl,
            @Value("${phantomtrace.threat-intel.virustotal.api-key:}") String apiKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.mapper = mapper;
        this.apiKey = apiKey;
    }

    @Override
    public ProviderLookup lookup(String url) {
        if (apiKey == null || apiKey.isBlank()) {
            return unavailable(url, "VirusTotal API credentials are not configured");
        }
        String id = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(url.getBytes(StandardCharsets.UTF_8));
        try {
            String body = client.get().uri("/urls/{id}", id).header("x-apikey", apiKey)
                    .retrieve().body(String.class);
            return body == null ? unavailable(url, "VirusTotal returned an empty response") : parse(url, body);
        } catch (ResourceAccessException ex) {
            return new ProviderLookup(url, "TIMEOUT", "UNAVAILABLE", null, null, "VirusTotal request timed out");
        } catch (RestClientResponseException ex) {
            return responseError(url, ex.getStatusCode().value());
        } catch (Exception ex) {
            return new ProviderLookup(url, "ERROR", "UNKNOWN", null, null, "VirusTotal lookup failed");
        }
    }

    private ProviderLookup parse(String url, String body) {
        try {
            JsonNode root = mapper.readTree(body);
            JsonNode attrs = root.path("data").path("attributes");
            JsonNode stats = attrs.path("last_analysis_stats");
            int malicious = stats.path("malicious").asInt(0);
            int suspicious = stats.path("suspicious").asInt(0);
            String status = malicious > 0 ? "MALICIOUS" : suspicious > 0 ? "SUSPICIOUS" : "NO_MALICIOUS_RESULT";
            return new ProviderLookup(url, "COMPLETED", status, stats.toString(),
                    root.path("data").path("id").asText(null), null);
        } catch (Exception ex) {
            return new ProviderLookup(url, "ERROR", "UNKNOWN", null, null, "Malformed VirusTotal response");
        }
    }

    private ProviderLookup unavailable(String url, String error) {
        return new ProviderLookup(url, "UNAVAILABLE", "UNAVAILABLE", null, null, error);
    }

    private ProviderLookup responseError(String url, int status) {
        if (status == 401 || status == 403) {
            return new ProviderLookup(url, "AUTHENTICATION_ERROR", "UNAVAILABLE", null, null,
                    "VirusTotal credentials were rejected");
        }
        if (status == 429) {
            return new ProviderLookup(url, "RATE_LIMITED", "UNAVAILABLE", null, null,
                    "VirusTotal rate limit reached");
        }
        if (status == 404) {
            return new ProviderLookup(url, "NOT_FOUND", "NOT_FOUND", null, null, null);
        }
        return new ProviderLookup(url, "ERROR", "UNKNOWN", null, null, "VirusTotal provider error");
    }
}
