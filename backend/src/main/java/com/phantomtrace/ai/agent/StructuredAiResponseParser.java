package com.phantomtrace.ai.agent;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.phantomtrace.ai.dto.StructuredAiResponse;
import com.phantomtrace.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StructuredAiResponseParser {
    private final ObjectMapper mapper = JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true).build();

    public StructuredAiResponse parse(String json, String expectedAgent) {
        try {
            StructuredAiResponse result = mapper.readValue(json, StructuredAiResponse.class);
            if (result.agentName() == null || !expectedAgent.equals(result.agentName())
                    || result.findings() == null || result.indicators() == null
                    || result.limitations() == null) {
                throw invalid();
            }
            result.findings().forEach(finding -> {
                if (finding.title() == null || finding.description() == null
                        || finding.severity() == null || finding.confidence() < 0
                        || finding.confidence() > 1 || finding.evidenceReference() == null
                        || finding.explanation() == null || finding.recommendedAction() == null
                        || finding.sourceType() == null) {
                    throw invalid();
                }
            });
            return result;
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw invalid();
        }
    }

    private ApiException invalid() {
        return new ApiException("AI response failed structured schema validation", HttpStatus.BAD_GATEWAY);
    }
}
