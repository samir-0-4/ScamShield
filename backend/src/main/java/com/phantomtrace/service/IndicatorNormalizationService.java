package com.phantomtrace.service;

import com.phantomtrace.enums.IndicatorType;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class IndicatorNormalizationService {
    public String normalize(IndicatorType type, String value) {
        if (value == null) return "";
        String normalized = value.trim();
        return switch (type) {
            case URL -> normalized.toLowerCase(Locale.ROOT);
            case DOMAIN, EMAIL_ADDRESS, IP_ADDRESS -> normalized.toLowerCase(Locale.ROOT);
            case PHONE_NUMBER -> normalized.replaceAll("[^0-9+]", "");
            default -> normalized.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        };
    }
}
