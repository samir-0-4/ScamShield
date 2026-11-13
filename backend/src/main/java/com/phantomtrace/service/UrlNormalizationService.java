package com.phantomtrace.service;

import com.phantomtrace.exception.ApiException;
import org.springframework.stereotype.Service;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;

@Service
public class UrlNormalizationService {

    public Optional<NormalizedUrl> normalize(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            URI parsed = new URI(value.trim());
            String scheme = parsed.getScheme();
            String host = parsed.getHost();
            if (host == null && parsed.getRawAuthority() != null) {
                host = parsed.getRawAuthority();
                int userInfoEnd = host.lastIndexOf('@');
                if (userInfoEnd >= 0) {
                    host = host.substring(userInfoEnd + 1);
                }
                int portStart = host.lastIndexOf(':');
                if (portStart > 0 && host.indexOf(']') < portStart) {
                    host = host.substring(0, portStart);
                }
            }
            if (scheme == null || host == null
                    || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                return Optional.empty();
            }

            String asciiHost = IDN.toASCII(host).toLowerCase(Locale.ROOT);
            StringBuilder normalized = new StringBuilder()
                    .append(scheme.toLowerCase(Locale.ROOT))
                    .append("://");
            if (parsed.getRawUserInfo() != null) {
                normalized.append(parsed.getRawUserInfo()).append('@');
            }
            normalized.append(asciiHost);
            if (parsed.getPort() != -1) {
                normalized.append(':').append(parsed.getPort());
            }
            if (parsed.getRawPath() != null) {
                normalized.append(parsed.getRawPath());
            }
            if (parsed.getRawQuery() != null) {
                normalized.append('?').append(parsed.getRawQuery());
            }
            if (parsed.getRawFragment() != null) {
                normalized.append('#').append(parsed.getRawFragment());
            }
            return Optional.of(new NormalizedUrl(value.trim(), normalized.toString(), asciiHost));
        } catch (URISyntaxException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public NormalizedUrl requireValid(String value) {
        return normalize(value).orElseThrow(() ->
                new ApiException("URL must be a valid HTTP or HTTPS address"));
    }

    public record NormalizedUrl(String original, String normalized, String domain) {
    }
}
