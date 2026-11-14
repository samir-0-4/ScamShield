package com.phantomtrace.util;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for detecting synthetic demonstration indicators and RFC-reserved testing namespaces.
 * <p>
 * Distinguishes between:
 * <ol>
 *   <li><b>Explicit Synthetic Demo Fixtures:</b> RFC 5737 TEST-NET IPs (192.0.2.0/24, 198.51.100.0/24,
 *       203.0.113.0/24), RFC 2606 special-use testing TLDs (.test, .example, .invalid, .localhost), and
 *       internal demonstration namespaces (*.internal-demo.test). For these fixtures, external threat-intelligence
 *       queries are bypassed (returning UNKNOWN with SYNTHETIC_TEST_INDICATOR) and demo campaign isolation applies.</li>
 *   <li><b>Reserved Documentation Example Domains:</b> RFC 2606 Section 3 reserved second-level domains
 *       (example.com, example.net, example.org). In genuine investigations, these domains do NOT suppress
 *       threat-intelligence queries and do NOT trigger synthetic demo campaign isolation.
 *       Note: example.edu is NOT an RFC 2606 reserved domain.</li>
 * </ol>
 */
public final class SyntheticIndicatorDetector {

    public static final String REASON_SYNTHETIC_TEST_INDICATOR = "SYNTHETIC_TEST_INDICATOR";

    private static final Pattern IPV4_PATTERN = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");

    private SyntheticIndicatorDetector() {
        // Utility class
    }

    /**
     * Checks if the indicator value is an explicit synthetic demonstration fixture:
     * - RFC 5737 TEST-NET IPv4 address
     * - RFC 2606 special-use test TLD (.test, .example, .invalid, .localhost)
     * - Internal demo namespace (*.internal-demo.test)
     *
     * @param value raw indicator string (URL, domain, IP address, or email)
     * @return true if an explicit synthetic demo fixture; false otherwise
     */
    public static boolean isExplicitSyntheticDemoFixture(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String candidate = extractHostOrDomain(value.trim());
        if (candidate == null || candidate.isBlank()) {
            return false;
        }

        String lower = candidate.toLowerCase(Locale.ROOT);

        // 1. RFC 5737 TEST-NET IPv4
        if (isRfc5737TestNetIp(lower)) {
            return true;
        }

        // 2. RFC 2606 special-use testing TLDs and internal demo domain
        return isExplicitTestTldOrNamespace(lower);
    }

    /**
     * Checks if the indicator is an RFC 2606 Section 3 reserved documentation domain:
     * example.com, example.net, or example.org (and subdomains).
     * <p>
     * Note: example.edu is intentionally NOT included because RFC 2606 only reserves .com, .net, and .org.
     *
     * @param value raw indicator string
     * @return true if an RFC 2606 reserved documentation domain; false otherwise
     */
    public static boolean isReservedExampleDomain(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String candidate = extractHostOrDomain(value.trim());
        if (candidate == null || candidate.isBlank()) {
            return false;
        }

        String host = candidate.toLowerCase(Locale.ROOT);

        return host.equals("example.com") || host.endsWith(".example.com")
                || host.equals("example.net") || host.endsWith(".example.net")
                || host.equals("example.org") || host.endsWith(".example.org");
    }

    /**
     * Identifies synthetic demonstration fixtures.
     * Delegates to {@link #isExplicitSyntheticDemoFixture(String)} to ensure that genuine
     * investigations containing documentation domains (example.com, etc.) are NOT treated as synthetic.
     */
    public static boolean isSyntheticIndicator(String value) {
        return isExplicitSyntheticDemoFixture(value);
    }

    /**
     * Checks if the given host is within any RFC 5737 TEST-NET IPv4 block:
     * <ul>
     *   <li>TEST-NET-1: 192.0.2.0/24</li>
     *   <li>TEST-NET-2: 198.51.100.0/24</li>
     *   <li>TEST-NET-3: 203.0.113.0/24</li>
     * </ul>
     */
    public static boolean isRfc5737TestNetIp(String host) {
        if (host == null) {
            return false;
        }
        Matcher matcher = IPV4_PATTERN.matcher(host);
        if (!matcher.matches()) {
            return false;
        }

        try {
            int octet1 = Integer.parseInt(matcher.group(1));
            int octet2 = Integer.parseInt(matcher.group(2));
            int octet3 = Integer.parseInt(matcher.group(3));
            int octet4 = Integer.parseInt(matcher.group(4));

            if (octet1 < 0 || octet1 > 255 || octet2 < 0 || octet2 > 255
                    || octet3 < 0 || octet3 > 255 || octet4 < 0 || octet4 > 255) {
                return false;
            }

            // TEST-NET-1: 192.0.2.0/24
            if (octet1 == 192 && octet2 == 0 && octet3 == 2) {
                return true;
            }
            // TEST-NET-2: 198.51.100.0/24
            if (octet1 == 198 && octet2 == 51 && octet3 == 100) {
                return true;
            }
            // TEST-NET-3: 203.0.113.0/24
            if (octet1 == 203 && octet2 == 0 && octet3 == 113) {
                return true;
            }
        } catch (NumberFormatException ignored) {
            return false;
        }

        return false;
    }

    /**
     * Checks if the host matches RFC 2606 special-use testing TLDs or internal demo domains.
     * (.test, .example, .invalid, .localhost, *.internal-demo.test)
     */
    public static boolean isExplicitTestTldOrNamespace(String host) {
        if (host == null || host.isBlank()) {
            return false;
        }

        return host.equals("test") || host.endsWith(".test")
                || host.equals("example") || host.endsWith(".example")
                || host.equals("invalid") || host.endsWith(".invalid")
                || host.equals("localhost") || host.endsWith(".localhost");
    }

    /**
     * Extracts host, hostname, or domain from URLs, emails, authority strings, or raw IP/domain strings.
     */
    public static String extractHostOrDomain(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();

        // If email, take domain part
        int atIndex = s.lastIndexOf('@');
        if (atIndex >= 0 && !s.contains("://")) {
            s = s.substring(atIndex + 1).trim();
        }

        // If contains URL scheme (e.g. http://, https://)
        if (s.contains("://")) {
            try {
                URI uri = URI.create(s);
                String host = uri.getHost();
                if (host != null && !host.isBlank()) {
                    return host;
                }
            } catch (Exception ignored) {
                // URI parse failed; fallback to manual extraction
            }
            // Strip scheme
            int schemeEnd = s.indexOf("://");
            s = s.substring(schemeEnd + 3);
        }

        // Strip path, query, fragment
        int slashIdx = s.indexOf('/');
        if (slashIdx >= 0) {
            s = s.substring(0, slashIdx);
        }
        int queryIdx = s.indexOf('?');
        if (queryIdx >= 0) {
            s = s.substring(0, queryIdx);
        }
        int fragIdx = s.indexOf('#');
        if (fragIdx >= 0) {
            s = s.substring(0, fragIdx);
        }

        // Strip port if present (avoid stripping IPv6 if bracketed)
        if (s.startsWith("[") && s.contains("]")) {
            int closeBracket = s.indexOf(']');
            return s.substring(1, closeBracket);
        }

        int colonIdx = s.lastIndexOf(':');
        if (colonIdx >= 0 && colonIdx == s.indexOf(':')) {
            // Exactly one colon, likely host:port
            String portPart = s.substring(colonIdx + 1);
            if (portPart.matches("^\\d+$")) {
                s = s.substring(0, colonIdx);
            }
        }

        return s;
    }
}
