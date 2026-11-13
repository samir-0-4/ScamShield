package com.phantomtrace.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter();
        ReflectionTestUtils.setField(filter, "enabled", true);
        chain = mock(FilterChain.class);
    }

    @Test
    void limitsRegistrationAndLoginAttempts() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(request("/api/v1/auth/register", "198.51.100.10", null).getStatus())
                    .isEqualTo(200);
        }
        MockHttpServletResponse rejectedRegister = request(
                "/api/v1/auth/register", "198.51.100.10", null);
        assertThat(rejectedRegister.getStatus()).isEqualTo(429);
        assertThat(rejectedRegister.getHeader("Retry-After")).isEqualTo("60");

        for (int i = 0; i < 10; i++) {
            assertThat(request("/api/v1/auth/login", "198.51.100.10", null).getStatus())
                    .isEqualTo(200);
        }
        MockHttpServletResponse rejectedLogin = request("/api/v1/auth/login", "198.51.100.10", null);
        assertThat(rejectedLogin.getStatus()).isEqualTo(429);
        verify(chain, times(15)).doFilter(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ignoresSpoofedForwardedForHeadersWhenApplyingLimit() throws Exception {
        for (int i = 0; i < 5; i++) {
            request("/api/v1/auth/register", "198.51.100.20", "10.0.0." + i);
        }

        MockHttpServletResponse rejected = request(
                "/api/v1/auth/register", "198.51.100.20", "203.0.113.99");

        assertThat(rejected.getStatus()).isEqualTo(429);
        verify(chain, times(5)).doFilter(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private MockHttpServletResponse request(String path, String remoteAddress, String forwardedFor)
            throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr(remoteAddress);
        if (forwardedFor != null) {
            request.addHeader("X-Forwarded-For", forwardedFor);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, chain);
        return response;
    }
}
