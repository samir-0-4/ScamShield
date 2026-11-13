package com.phantomtrace.integration.virustotal;

public interface VirusTotalClient {
    ProviderLookup lookup(String url);

    record ProviderLookup(String indicator, String lookupStatus, String threatStatus,
            String details, String reference, String error) {
    }
}
