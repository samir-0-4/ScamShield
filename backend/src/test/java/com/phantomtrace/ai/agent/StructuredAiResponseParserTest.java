package com.phantomtrace.ai.agent;

import com.phantomtrace.exception.ApiException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredAiResponseParserTest {
    private final StructuredAiResponseParser parser = new StructuredAiResponseParser();

    @Test
    void rejectsMalformedAndUnexpectedStructuredResponses() {
        assertThatThrownBy(() -> parser.parse("not-json", "TEXT_DETECTIVE"))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> parser.parse("""
                {"agentName":"TEXT_DETECTIVE","findings":[{"title":"x","description":"y","severity":"HIGH",
                "confidence":2,"verificationStatus":"UNVERIFIED","evidenceReference":"e1",
                "explanation":"z","recommendedAction":"a","sourceType":"AI"}],"indicators":[],"summary":"",
                "limitations":[]}
                """, "TEXT_DETECTIVE")).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> parser.parse("""
                {"agentName":"OTHER","findings":[],"indicators":[],"summary":"","limitations":[]}
                """, "TEXT_DETECTIVE")).isInstanceOf(ApiException.class);
    }
}
