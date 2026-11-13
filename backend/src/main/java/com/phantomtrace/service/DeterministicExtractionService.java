package com.phantomtrace.service;

import com.phantomtrace.dto.evidence.ExtractedIndicatorResponse;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class DeterministicExtractionService {

    private static final String METHOD = "DETERMINISTIC_REGEX_RULES_V1";
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)https?://[^\\s<>\"']+");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern IP_PATTERN = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?<!\\w)(?:\\+?\\d[\\d ()-]{7,}\\d)(?!\\w)");

    private static final Map<IndicatorType, List<String>> TERM_RULES = Map.of(
            IndicatorType.PAYMENT_TERM, List.of(
                    "pay", "payment", "transfer", "fee", "deposit", "wire", "bank details",
                    "gift card", "crypto", "bitcoin", "registration fee", "processing fee"),
            IndicatorType.CREDENTIAL_TERM, List.of(
                    "password", "passcode", "login", "username", "security question", "verify your account"),
            IndicatorType.OTP_TERM, List.of(
                    "otp", "one-time password", "one time password", "verification code", "security code"),
            IndicatorType.SUSPICIOUS_PHRASE, List.of(
                    "guaranteed income", "work from home", "act as my agent", "keep this confidential",
                    "click the link below", "you have been selected"),
            IndicatorType.URGENCY_INDICATOR, List.of(
                    "urgent", "immediately", "right now", "within 24 hours", "today only",
                    "last chance", "expires", "deadline"));

    private static final Map<String, String> SIGNAL_RULES = Map.of(
            "UPFRONT_PAYMENT_REQUEST", "(?i)(pay|send|transfer|deposit).{0,40}(fee|money|payment|amount|cost)",
            "REGISTRATION_FEE", "(?i)(registration|processing|application|training).{0,30}fee",
            "OTP_REQUEST", "(?i)(share|send|provide|tell me|enter).{0,30}(otp|one[- ]time password|verification code|security code)",
            "PASSWORD_REQUEST", "(?i)(share|send|provide|tell me|confirm).{0,30}(password|passcode|login)",
            "ACCOUNT_SUSPENSION_THREAT", "(?i)(account|profile).{0,30}(suspend|locked|close|terminate)",
            "URGENCY_DEADLINE", "(?i)(urgent|immediately|right now|within 24 hours|deadline|expires)",
            "UNREALISTIC_OFFER", "(?i)(guaranteed|risk[- ]free|double your money|easy money|no experience).{0,40}(income|profit|pay|salary|money)?",
            "SUSPICIOUS_RECRUITMENT", "(?i)(job|work|hiring|recruit).{0,40}(fee|deposit|pay|registration|whatsapp|telegram)",
            "PAYMENT_PRESSURE", "(?i)(must|need to|have to).{0,20}(pay|send|transfer|deposit)",
            "IMPERSONATION_INDICATOR", "(?i)(this is|i am).{0,25}(your bank|support team|police|government|manager|boss)");

    private final ExtractedIndicatorRepository repository;
    private final UrlNormalizationService urlNormalizationService;

    public DeterministicExtractionService(ExtractedIndicatorRepository repository,
            UrlNormalizationService urlNormalizationService) {
        this.repository = repository;
        this.urlNormalizationService = urlNormalizationService;
    }

    public List<ExtractedIndicatorResponse> extractAndPersist(Evidence evidence) {
        String content = sourceContent(evidence);
        if (content.isBlank() && evidence.getEvidenceType() != EvidenceType.URL) {
            return List.of();
        }

        Map<String, Candidate> candidates = new LinkedHashMap<>();
        extractUrls(content, candidates);
        extractPattern(content, EMAIL_PATTERN, IndicatorType.EMAIL_ADDRESS, candidates, true);
        extractPattern(content, IP_PATTERN, IndicatorType.IP_ADDRESS, candidates, true);
        extractPattern(content, PHONE_PATTERN, IndicatorType.PHONE_NUMBER, candidates, true);
        extractTerms(content, candidates);
        extractSignals(content, candidates);

        if (evidence.getEvidenceType() == EvidenceType.URL && evidence.getUrl() != null) {
            addUrl(evidence.getUrl(), candidates);
        }

        Instant extractedAt = Instant.now();
        List<ExtractedIndicator> persisted = candidates.values().stream()
                .map(candidate -> new ExtractedIndicator(evidence, candidate.type(), candidate.originalValue(),
                        candidate.normalizedValue(), METHOD, extractedAt))
                .map(repository::save)
                .toList();
        return persisted.stream().map(ExtractedIndicatorResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<ExtractedIndicatorResponse> list(UUID evidenceId) {
        return repository.findByEvidenceIdOrderByExtractedAtAsc(evidenceId).stream()
                .map(ExtractedIndicatorResponse::fromEntity)
                .toList();
    }

    private void extractUrls(String content, Map<String, Candidate> candidates) {
        Matcher matcher = URL_PATTERN.matcher(content);
        while (matcher.find()) {
            addUrl(trimUrlPunctuation(matcher.group()), candidates);
        }
    }

    private void addUrl(String original, Map<String, Candidate> candidates) {
        urlNormalizationService.normalize(original).ifPresent(url -> {
            add(candidates, IndicatorType.URL, url.original(), url.normalized());
            add(candidates, IndicatorType.DOMAIN, url.domain(), url.domain());
        });
    }

    private void extractPattern(String content, Pattern pattern, IndicatorType type,
            Map<String, Candidate> candidates, boolean lowerNormalized) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String original = matcher.group().trim();
            add(candidates, type, original, lowerNormalized ? original.toLowerCase(Locale.ROOT) : original);
        }
    }

    private void extractTerms(String content, Map<String, Candidate> candidates) {
        for (Map.Entry<IndicatorType, List<String>> rule : TERM_RULES.entrySet()) {
            for (String term : rule.getValue()) {
                Matcher matcher = Pattern.compile("(?i)(?<!\\w)" + Pattern.quote(term) + "(?!\\w)")
                        .matcher(content);
                while (matcher.find()) {
                    add(candidates, rule.getKey(), matcher.group(), term.toLowerCase(Locale.ROOT));
                }
            }
        }
    }

    private void extractSignals(String content, Map<String, Candidate> candidates) {
        SIGNAL_RULES.forEach((name, expression) -> {
            Matcher matcher = Pattern.compile(expression).matcher(content);
            if (matcher.find()) {
                add(candidates, IndicatorType.MESSAGE_SIGNAL, matcher.group(), name);
            }
        });
    }

    private void add(Map<String, Candidate> candidates, IndicatorType type, String original, String normalized) {
        String key = type.name() + "\u0000" + original + "\u0000" + normalized;
        candidates.putIfAbsent(key, new Candidate(type, original, normalized));
    }

    private String sourceContent(Evidence evidence) {
        List<String> fields = new ArrayList<>();
        if (evidence.getTextContent() != null) {
            fields.add(evidence.getTextContent());
        }
        if (evidence.getUrl() != null) {
            fields.add(evidence.getUrl());
        }
        if (evidence.getSenderAddress() != null) {
            fields.add(evidence.getSenderAddress());
        }
        if (evidence.getSubject() != null) {
            fields.add(evidence.getSubject());
        }
        if (evidence.getEmailBody() != null) {
            fields.add(evidence.getEmailBody());
        }
        if (evidence.getEmailHeaders() != null) {
            fields.add(evidence.getEmailHeaders());
        }
        if (evidence.getNormalizedContent() != null) {
            fields.add(evidence.getNormalizedContent());
        }
        return String.join("\n", fields);
    }

    private String trimUrlPunctuation(String value) {
        return value.replaceFirst("[.,!?;:]+$", "").replaceFirst("\\)$", "");
    }

    private record Candidate(IndicatorType type, String originalValue, String normalizedValue) {
    }
}
