package com.phantomtrace.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limiting filter using Bucket4j in-memory buckets, keyed by remote IP + path bucket.
 *
 * Rules:
 *   /api/v1/evidence/**        — 20 requests per minute per IP
 *   /api/v1/investigations/**  — 20 requests per minute per IP
 *   /api/v1/auth/register      — 5 requests per minute per IP
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final int GENERAL_API_RPM = 20;
    private static final int REGISTER_RPM = 5;
    private static final int LOGIN_RPM = 10;
    private static final int MAX_BUCKETS = 10_000;

    @Value("${phantomtrace.security.rate-limit.enabled:true}")
    private boolean enabled;

    // Buckets keyed by "bucketKey:ip"
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String ip = resolveClientIp(request);

        String bucketKey = resolveBucketKey(path);
        if (bucketKey == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(bucketKey + ":" + ip, k -> createBucket(bucketKey));
        evictIfOversized();

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP={} path={}", ip, path);
            sendRateLimitExceeded(response);
        }
    }

    private String resolveBucketKey(String path) {
        if (path.equals("/api/v1/auth/register")) {
            return "register";
        }
        if (path.equals("/api/v1/auth/login")) {
            return "login";
        }
        if (path.startsWith("/api/v1/evidence/") || path.equals("/api/v1/evidence")) {
            return "general";
        }
        if (path.startsWith("/api/v1/investigations/") || path.equals("/api/v1/investigations")) {
            return "general";
        }
        return null;
    }

    private Bucket createBucket(String bucketKey) {
        int rpm = switch (bucketKey) {
            case "register" -> REGISTER_RPM;
            case "login" -> LOGIN_RPM;
            default -> GENERAL_API_RPM;
        };
        Bandwidth limit = Bandwidth.builder()
                .capacity(rpm)
                .refillGreedy(rpm, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    private void sendRateLimitExceeded(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", "60");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> body = Map.of(
                "error", "RATE_LIMIT_EXCEEDED",
                "message", "Too many requests. Please slow down and try again later.",
                "timestamp", Instant.now().toString()
        );
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void evictIfOversized() {
        if (buckets.size() <= MAX_BUCKETS) {
            return;
        }
        int target = MAX_BUCKETS / 2;
        var iterator = buckets.keySet().iterator();
        while (buckets.size() > target && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
