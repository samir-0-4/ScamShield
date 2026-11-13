package com.phantomtrace.ai.agent;

import com.phantomtrace.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class SecurityManager {

    private static final int MAX_INPUT = 24000;
    private static final int MAX_URL_LENGTH = 2048;

    // SSRF block patterns
    private static final Pattern PRIVATE_10 = Pattern.compile("^10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private static final Pattern PRIVATE_172 = Pattern.compile("^172\\.(1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3}$");
    private static final Pattern PRIVATE_192 = Pattern.compile("^192\\.168\\.\\d{1,3}\\.\\d{1,3}$");
    private static final Pattern LINK_LOCAL = Pattern.compile("^169\\.254\\.\\d{1,3}\\.\\d{1,3}$");

    private static final java.util.Set<String> BLOCKED_HOSTS = java.util.Set.of(
            "localhost", "127.0.0.1", "0.0.0.0", "::1",
            "169.254.169.254",         // AWS/GCP/Azure metadata
            "[::1]", "0:0:0:0:0:0:0:1"
    );

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

    /**
     * Validates that a URL does not point to a private/internal resource (SSRF protection).
     * Throws ApiException if the URL is blocked.
     */
    public void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ApiException("URL must not be blank", HttpStatus.BAD_REQUEST);
        }
        if (url.length() > MAX_URL_LENGTH) {
            throw new ApiException("URL exceeds the maximum allowed length", HttpStatus.BAD_REQUEST);
        }
        try {
            URI uri = URI.create(url);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ApiException("Only HTTP and HTTPS URLs are allowed", HttpStatus.BAD_REQUEST);
            }
            if (uri.getRawUserInfo() != null || uri.getRawFragment() != null) {
                throw new ApiException("URL must not contain credentials or fragments", HttpStatus.BAD_REQUEST);
            }
            String host = uri.getHost();
            if (host == null) {
                throw new ApiException("URL has no resolvable host", HttpStatus.BAD_REQUEST);
            }
            if (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443) {
                throw new ApiException("Only standard HTTP and HTTPS ports are allowed", HttpStatus.BAD_REQUEST);
            }
            String normalized = host.toLowerCase(Locale.ROOT).replace("[", "").replace("]", ".");
            if (BLOCKED_HOSTS.contains(normalized) || BLOCKED_HOSTS.contains(host.toLowerCase(Locale.ROOT))) {
                throw new ApiException("URL resolves to a blocked or private address", HttpStatus.BAD_REQUEST);
            }
            if (PRIVATE_10.matcher(normalized).matches()
                    || PRIVATE_172.matcher(normalized).matches()
                    || PRIVATE_192.matcher(normalized).matches()
                    || LINK_LOCAL.matcher(normalized).matches()) {
                throw new ApiException("URL resolves to a blocked or private address", HttpStatus.BAD_REQUEST);
            }
            // IPv6 unique-local addresses (fc00::/7).
            if (normalized.startsWith("fc") || normalized.startsWith("fd")) {
                throw new ApiException("URL resolves to a blocked or private address", HttpStatus.BAD_REQUEST);
            }
            if (host.toLowerCase(Locale.ROOT).endsWith(".invalid")) {
                throw new ApiException("URL host could not be resolved", HttpStatus.BAD_REQUEST);
            }
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                for (InetAddress addr : addresses) {
                    if (isBlockedAddress(addr)) {
                        throw new ApiException("URL resolves to a blocked or private address", HttpStatus.BAD_REQUEST);
                    }
                }
            } catch (UnknownHostException ex) {
                // Host could not be resolved (NXDOMAIN, offline domain, or network sandbox).
                // It does not point to an internal private address, so allow as evidence for analysis.
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Invalid URL: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isBlockedAddress(InetAddress address) {
        if (address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isAnyLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet6Address && bytes.length == 16) {
            // fc00::/7 (ULA), plus IPv4-mapped private/link-local addresses.
            if ((bytes[0] & 0xff) == 0xfc || (bytes[0] & 0xff) == 0xfd) {
                return true;
            }
            if (bytes[0] == 0 && bytes[1] == 0 && bytes[2] == 0 && bytes[3] == 0
                    && bytes[4] == 0 && bytes[5] == 0 && bytes[6] == 0 && bytes[7] == 0
                    && bytes[8] == 0 && bytes[9] == 0 && bytes[10] == 0 && bytes[11] == 0
                    && (bytes[12] & 0xff) == 0xff && (bytes[13] & 0xff) == 0xff) {
                String mappedIpv4 = (bytes[12] & 0xff) + "." + (bytes[13] & 0xff) + "."
                        + (bytes[14] & 0xff) + "." + (bytes[15] & 0xff);
                return PRIVATE_10.matcher(mappedIpv4).matches()
                        || PRIVATE_172.matcher(mappedIpv4).matches()
                        || PRIVATE_192.matcher(mappedIpv4).matches()
                        || LINK_LOCAL.matcher(mappedIpv4).matches();
            }
        }
        return false;
    }
}
