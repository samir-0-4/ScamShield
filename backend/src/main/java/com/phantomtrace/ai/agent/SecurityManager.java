package com.phantomtrace.ai.agent;

import org.springframework.stereotype.Component;

@Component
public class SecurityManager {

    private static final int MAX_INPUT = 24000;

    public String sanitizeUserInput(String input) {
        if (input == null) return "";
        String bounded = input.length() > MAX_INPUT ? input.substring(0, MAX_INPUT) : input;
        return bounded.replace("\u0000", " ").replace("```", "'''");
    }

    public boolean validateOutputSchema(String jsonOutput) {
        return jsonOutput != null && !jsonOutput.isBlank()
                && jsonOutput.trim().startsWith("{") && jsonOutput.trim().endsWith("}");
    }

    public String wrapUntrusted(String label, String value) {
        return "\n<untrusted-" + label + ">\n" + sanitizeUserInput(value)
                + "\n</untrusted-" + label + ">\n";
    }
}
