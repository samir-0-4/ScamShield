package com.phantomtrace.integration.urlhaus;

public interface UrlhausClient {
    ProviderLookup lookup(String url);

    record ProviderLookup(String indicator, String lookupStatus, String threatStatus,
            String details, String reference, String error) {
    }
}
