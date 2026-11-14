package com.phantomtrace.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SyntheticIndicatorDetectorTest {

    // =========================================================================
    // RFC 5737 IPv4 TEST-NET Range Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "192.0.2.0",
            "192.0.2.1",
            "192.0.2.42",
            "192.0.2.255"
    })
    @DisplayName("Recognizes RFC 5737 TEST-NET-1 (192.0.2.0/24) IP addresses")
    void recognizesTestNet1Range(String ip) {
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(ip)).isTrue();
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(ip)).isTrue();
        assertThat(SyntheticIndicatorDetector.isRfc5737TestNetIp(ip)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "198.51.100.0",
            "198.51.100.1",
            "198.51.100.42",
            "198.51.100.255"
    })
    @DisplayName("Recognizes RFC 5737 TEST-NET-2 (198.51.100.0/24) IP addresses")
    void recognizesTestNet2Range(String ip) {
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(ip)).isTrue();
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(ip)).isTrue();
        assertThat(SyntheticIndicatorDetector.isRfc5737TestNetIp(ip)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "203.0.113.0",
            "203.0.113.1",
            "203.0.113.100",
            "203.0.113.255"
    })
    @DisplayName("Recognizes RFC 5737 TEST-NET-3 (203.0.113.0/24) IP addresses")
    void recognizesTestNet3Range(String ip) {
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(ip)).isTrue();
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(ip)).isTrue();
        assertThat(SyntheticIndicatorDetector.isRfc5737TestNetIp(ip)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "192.0.1.255",
            "192.0.3.0",
            "192.0.3.1",
            "198.51.99.255",
            "198.51.101.0",
            "198.50.100.42",
            "203.0.112.255",
            "203.0.114.0",
            "203.1.113.1",
            "8.8.8.8",
            "1.1.1.1",
            "127.0.0.1",
            "10.0.0.1",
            "172.16.0.1"
    })
    @DisplayName("Rejects IP addresses outside RFC 5737 TEST-NET ranges")
    void rejectsNonTestNetIps(String ip) {
        assertThat(SyntheticIndicatorDetector.isRfc5737TestNetIp(ip)).isFalse();
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(ip)).isFalse();
    }

    // =========================================================================
    // URL Format Tests for Explicit Synthetic Demo Fixtures
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "http://192.0.2.1/login",
            "https://192.0.2.254:8443/api/v1/auth?token=xyz#hash",
            "http://198.51.100.42/kyc-portal",
            "https://198.51.100.42:8080/refund?case=1",
            "http://203.0.113.100:9000/",
            "https://secure-netbanking-verify.internal-demo.test/login?case=49102",
            "http://auth.corp.internal-demo.test/verify",
            "http://portal.test/index.html",
            "https://malicious.example/path",
            "http://phish.invalid/token",
            "http://app.localhost:3000/callback"
    })
    @DisplayName("Correctly parses URLs and detects explicit synthetic demo fixtures")
    void detectsSyntheticUrls(String url) {
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(url)).isTrue();
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(url)).isTrue();
    }

    // =========================================================================
    // RFC 2606 Special-Use Test TLDs & Namespaces
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "internal-demo.test",
            "secure-netbanking-verify.internal-demo.test",
            "gateway.bank.test",
            "test",
            "foo.example",
            "example",
            "corp.invalid",
            "invalid",
            "localhost",
            "sub.localhost"
    })
    @DisplayName("Recognizes RFC 2606 special-use test TLDs and demo namespaces")
    void recognizesExplicitTestTlds(String domain) {
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(domain)).isTrue();
        assertThat(SyntheticIndicatorDetector.isExplicitTestTldOrNamespace(domain)).isTrue();
    }

    // =========================================================================
    // RFC 2606 Section 3 Reserved Documentation Example Domains
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "example.com",
            "bank.example.com",
            "https://example.com/docs",
            "example.net",
            "auth.example.net",
            "http://subdomain.example.net:8080/test",
            "example.org",
            "sub.example.org",
            "https://test.example.org/verify"
    })
    @DisplayName("Distinguishes RFC 2606 reserved example domains without treating them as explicit synthetic demo fixtures")
    void distinguishesReservedExampleDomains(String domainOrUrl) {
        // Must be identified as an RFC 2606 reserved documentation domain
        assertThat(SyntheticIndicatorDetector.isReservedExampleDomain(domainOrUrl)).isTrue();

        // Must NOT be treated as an explicit synthetic demo fixture that suppresses lookups or forces demo campaigns
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(domainOrUrl)).isFalse();
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(domainOrUrl)).isFalse();
    }

    // =========================================================================
    // Rejection of example.edu
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "example.edu",
            "sub.example.edu",
            "https://example.edu/index.html",
            "login.example.edu"
    })
    @DisplayName("example.edu is NOT an RFC 2606 reserved domain and is rejected")
    void rejectsExampleEdu(String domainOrUrl) {
        assertThat(SyntheticIndicatorDetector.isReservedExampleDomain(domainOrUrl)).isFalse();
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(domainOrUrl)).isFalse();
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(domainOrUrl)).isFalse();
    }

    // =========================================================================
    // Email Address Tests
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "compliance-desk@netbanking-alerts.internal-demo.test",
            "admin@secure.test",
            "alert@invalid",
            "fraud@198.51.100.42",
            "alert@203.0.113.5"
    })
    @DisplayName("Extracts domain/IP from email addresses and identifies explicit synthetic demo fixtures")
    void recognizesSyntheticEmails(String email) {
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(email)).isTrue();
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(email)).isTrue();
    }

    @Test
    @DisplayName("Email with example.com is recognized as reserved documentation domain but not synthetic demo fixture")
    void recognizesExampleComEmail() {
        assertThat(SyntheticIndicatorDetector.isReservedExampleDomain("user@example.com")).isTrue();
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture("user@example.com")).isFalse();
    }

    // =========================================================================
    // Negative Tests: Arbitrary Strings and Real-World Domains Must NOT Match
    // =========================================================================

    @ParameterizedTest
    @ValueSource(strings = {
            "speedtest.net",
            "test.com",
            "contest.org",
            "contest.net",
            "mytestserver.com",
            "testing-corp.io",
            "latestnews.com",
            "attestation.gov",
            "evil-test.invalid.com", // TLD is .com, not .invalid
            "sub.test.co.uk",       // ccTLD is .uk, not .test
            "example.co.uk",        // not example.com/net/org
            "https://evil-bank.com/test", // URL path contains 'test' but host is evil-bank.com
            "https://contest.org/198.51.100.42", // Path contains IP, host is contest.org
            "https://192.0.3.1/login", // IP outside RFC 5737
            "https://8.8.8.8/dns-query",
            "user@gmail.com",
            "phish@evil-bank.com",
            "",
            "   "
    })
    @DisplayName("Rejects arbitrary unrelated strings and legitimate external domains")
    void rejectsUnrelatedStrings(String input) {
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(input)).isFalse();
        assertThat(SyntheticIndicatorDetector.isReservedExampleDomain(input)).isFalse();
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(input)).isFalse();
    }

    @Test
    @DisplayName("Null input returns false safely")
    void handlesNullSafely() {
        assertThat(SyntheticIndicatorDetector.isSyntheticIndicator(null)).isFalse();
        assertThat(SyntheticIndicatorDetector.isExplicitSyntheticDemoFixture(null)).isFalse();
        assertThat(SyntheticIndicatorDetector.isReservedExampleDomain(null)).isFalse();
        assertThat(SyntheticIndicatorDetector.isRfc5737TestNetIp(null)).isFalse();
    }
}
