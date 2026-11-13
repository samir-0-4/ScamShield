package com.phantomtrace.integration.urlhaus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;

@Component
public class UrlhausClientImpl implements UrlhausClient {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String apiKey;

    public UrlhausClientImpl(ObjectMapper mapper,
            @Value("${phantomtrace.threat-intel.urlhaus.base-url:https://urlhaus-api.abuse.ch/v1}") String baseUrl,
            @Value("${phantomtrace.threat-intel.urlhaus.api-key:}") String apiKey) {
        this.client = RestClient.builder().baseUrl(baseUrl).build();
        this.mapper = mapper;
        this.apiKey = apiKey;
    }

    @Override
    public ProviderLookup lookup(String url) {
        if (apiKey == null || apiKey.isBlank()) {
            return new ProviderLookup(url, "UNAVAILABLE", "UNAVAILABLE", null, null,
                    "URLhaus API credentials are not configured");
        }
        try {
            String body = client.post().uri("/url/").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Auth-Key", apiKey).body("url=" + java.net.URLEncoder.encode(url, java.nio.charset.StandardCharsets.UTF_8))
                    .retrieve().body(String.class);
            JsonNode root = mapper.readTree(body);
            String queryStatus = root.path("query_status").asText("");
            if ("no_results".equalsIgnoreCase(queryStatus)) {
                return new ProviderLookup(url, "NOT_FOUND", "NOT_FOUND", queryStatus, null, null);
            }
            if (!"ok".equalsIgnoreCase(queryStatus)) {
                return new ProviderLookup(url, "COMPLETED", "UNKNOWN", root.toString(), null, queryStatus);
            }
            return new ProviderLookup(url, "COMPLETED", "MALICIOUS", root.toString(),
                    root.path("id").asText(null), null);
        } catch (ResourceAccessException ex) {
            return new ProviderLookup(url, "TIMEOUT", "UNAVAILABLE", null, null, "URLhaus request timed out");
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 429) {
                return new ProviderLookup(url, "RATE_LIMITED", "UNAVAILABLE", null, null,
                        "URLhaus rate limit reached");
            }
            if (ex.getStatusCode().value() == 401 || ex.getStatusCode().value() == 403) {
                return new ProviderLookup(url, "AUTHENTICATION_ERROR", "UNAVAILABLE", null, null,
                        "URLhaus credentials were rejected");
            }
            return new ProviderLookup(url, "ERROR", "UNKNOWN", null, null, "URLhaus provider error");
        } catch (Exception ex) {
            return new ProviderLookup(url, "ERROR", "UNKNOWN", null, null, "URLhaus lookup failed");
        }
    }
}
