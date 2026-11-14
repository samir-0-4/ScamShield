package com.phantomtrace.service;

import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.EvidenceRelationship;
import com.phantomtrace.entity.ExtractedIndicator;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.RiskAssessment;
import com.phantomtrace.entity.RiskContribution;
import com.phantomtrace.entity.ThreatLookup;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.IndicatorType;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.InvestigationStatus;
import com.phantomtrace.enums.LookupStatus;
import com.phantomtrace.enums.RelationshipType;
import com.phantomtrace.enums.RelationshipVerificationStatus;
import com.phantomtrace.enums.RiskCategory;
import com.phantomtrace.enums.SeverityLevel;
import com.phantomtrace.enums.ThreatStatus;
import com.phantomtrace.repository.EvidenceRelationshipRepository;
import com.phantomtrace.repository.EvidenceRepository;
import com.phantomtrace.repository.ExtractedIndicatorRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.repository.RiskAssessmentRepository;
import com.phantomtrace.repository.RiskContributionRepository;
import com.phantomtrace.repository.ThreatLookupRepository;
import com.phantomtrace.repository.UserRepository;
import com.phantomtrace.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DemoDatasetService {

    public static final String DEMO_PREFIX = "[DEMO] ";

    private final InvestigationRepository investigationRepository;
    private final EvidenceRepository evidenceRepository;
    private final ExtractedIndicatorRepository indicatorRepository;
    private final ThreatLookupRepository lookupRepository;
    private final RiskAssessmentRepository assessmentRepository;
    private final RiskContributionRepository contributionRepository;
    private final EvidenceRelationshipRepository relationshipRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public DemoDatasetService(InvestigationRepository investigationRepository,
                              EvidenceRepository evidenceRepository,
                              ExtractedIndicatorRepository indicatorRepository,
                              ThreatLookupRepository lookupRepository,
                              RiskAssessmentRepository assessmentRepository,
                              RiskContributionRepository contributionRepository,
                              EvidenceRelationshipRepository relationshipRepository,
                              UserRepository userRepository,
                              SecurityUtils securityUtils) {
        this.investigationRepository = investigationRepository;
        this.evidenceRepository = evidenceRepository;
        this.indicatorRepository = indicatorRepository;
        this.lookupRepository = lookupRepository;
        this.assessmentRepository = assessmentRepository;
        this.contributionRepository = contributionRepository;
        this.relationshipRepository = relationshipRepository;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    public List<Investigation> seedDemoCampaign() {
        UUID userId = securityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        // Clean up any prior demo cases for this user to ensure idempotency
        resetDemoCampaign();

        Instant now = Instant.now();
        List<Investigation> createdInvestigations = new ArrayList<>();

        // Shared Campaign Indicators (Apex Banking Fraud Campaign)
        String sharedDomain = "secure-netbanking-verify.internal-demo.test";
        String sharedIp = "185.220.101.5";

        // ==========================================
        // CASE 1: Urgent SMS Alert & Credential Harvesting
        // ==========================================
        Investigation case1 = new Investigation(
                currentUser,
                DEMO_PREFIX + "Apex Campaign — SMS Banking Alert & Account Suspension",
                "Incident Report: Victim received SMS claiming unauthorized login from IP " + sharedIp
                        + " and demanding immediate verification through a spoofed banking portal.",
                InvestigationCategory.PHISHING
        );
        case1.setStatus(InvestigationStatus.COMPLETED);
        case1.setRiskScore(85.0);
        case1.setOverallRiskLevel(SeverityLevel.CRITICAL);
        case1 = investigationRepository.save(case1);
        createdInvestigations.add(case1);

        Evidence ev1 = new Evidence(
                case1,
                EvidenceType.TEXT_MESSAGE,
                "Inbound SMS message captured on victim mobile device."
        );
        ev1.setTextContent("ALERT: Unusual login attempt detected on your NetBanking account from IP " + sharedIp
                + ". To prevent permanent suspension, verify your identity immediately: https://" + sharedDomain
                + "/login?case=49102. Do not share your OTP.");
        ev1.setStatus("COMPLETED");
        ev1 = evidenceRepository.save(ev1);

        ExtractedIndicator ind1Url = new ExtractedIndicator(ev1, IndicatorType.URL,
                "https://" + sharedDomain + "/login?case=49102",
                "https://" + sharedDomain + "/login?case=49102",
                "DETERMINISTIC_REGEX_V1", now.minus(2, ChronoUnit.HOURS));
        ind1Url = indicatorRepository.save(ind1Url);

        ExtractedIndicator ind1Domain = new ExtractedIndicator(ev1, IndicatorType.DOMAIN,
                sharedDomain, sharedDomain,
                "DETERMINISTIC_DOMAIN_EXTRACTOR", now.minus(2, ChronoUnit.HOURS));
        ind1Domain = indicatorRepository.save(ind1Domain);

        ExtractedIndicator ind1Ip = new ExtractedIndicator(ev1, IndicatorType.IP_ADDRESS,
                sharedIp, sharedIp,
                "DETERMINISTIC_IP_EXTRACTOR", now.minus(2, ChronoUnit.HOURS));
        ind1Ip = indicatorRepository.save(ind1Ip);

        ExtractedIndicator ind1Urgency = new ExtractedIndicator(ev1, IndicatorType.URGENCY_INDICATOR,
                "immediately", "immediately",
                "DETERMINISTIC_LEXICAL_EXTRACTOR", now.minus(2, ChronoUnit.HOURS));
        indicatorRepository.save(ind1Urgency);

        ExtractedIndicator ind1Otp = new ExtractedIndicator(ev1, IndicatorType.OTP_TERM,
                "otp", "otp",
                "DETERMINISTIC_LEXICAL_EXTRACTOR", now.minus(2, ChronoUnit.HOURS));
        indicatorRepository.save(ind1Otp);

        // Threat Lookup for Case 1
        ThreatLookup lookup1 = new ThreatLookup(
                ind1Url,
                "URLhaus (Synthetic Demonstration)",
                "https://" + sharedDomain + "/login?case=49102",
                LookupStatus.COMPLETED,
                ThreatStatus.MALICIOUS,
                "{\"threat\": \"credential_stealer\", \"signature\": \"ApexBankingTrojan\", \"confidence\": 98}",
                "URLHAUS-DEMO-2026-9941",
                null,
                now.minus(2, ChronoUnit.HOURS)
        );
        lookupRepository.save(lookup1);

        // Risk Assessment Case 1
        RiskAssessment risk1 = new RiskAssessment(
                case1,
                85,
                RiskCategory.CRITICAL,
                0.95,
                "All deterministic indicators and threat intelligence feeds resolved.",
                "High-confidence phishing campaign detected. Multiple credential theft terms, verified malicious URLhaus listing, and direct IP reference.",
                now.minus(2, ChronoUnit.HOURS)
        );
        risk1 = assessmentRepository.save(risk1);

        String ev1Ref = ev1.getId() != null ? ev1.getId().toString() : "DEMO-EV1";
        contributionRepository.save(new RiskContribution(risk1, "MALICIOUS_THREAT_INTEL", "Verified Malicious Threat Intel", 40, ev1Ref, "URLhaus verified indicator as active banking phishing landing page."));
        contributionRepository.save(new RiskContribution(risk1, "CREDENTIAL_OTP", "Credential and OTP Harvester", 25, ev1Ref, "Explicit solicitation of authentication credentials and one-time passcodes."));
        contributionRepository.save(new RiskContribution(risk1, "URGENCY_PRESSURE", "Urgency and Suspension Threat", 20, ev1Ref, "High-pressure coercion language threatening account deactivation."));

        // ==========================================
        // CASE 2: Spear-Phishing Email & KYC Lure
        // ==========================================
        Investigation case2 = new Investigation(
                currentUser,
                DEMO_PREFIX + "Apex Campaign — Spear-Phishing Email & KYC Lure",
                "Corporate Security Alert: Phishing email addressed to accounting staff requesting annual KYC compliance disclosure on identical host " + sharedDomain + ".",
                InvestigationCategory.PHISHING
        );
        case2.setStatus(InvestigationStatus.COMPLETED);
        case2.setRiskScore(75.0);
        case2.setOverallRiskLevel(SeverityLevel.HIGH);
        case2 = investigationRepository.save(case2);
        createdInvestigations.add(case2);

        Evidence ev2 = new Evidence(
                case2,
                EvidenceType.EMAIL,
                "Inbound spear-phishing email received by staff member."
        );
        ev2.setSenderAddress("compliance-desk@netbanking-alerts.internal-demo.test");
        ev2.setSubject("Action Required: Mandatory Annual KYC Update for Account Holder");
        ev2.setEmailBody("Dear Customer, Your NetBanking profile has pending compliance disclosures. Failure to update within 24 hours will result in card deactivation. Access the verification portal: https://" + sharedDomain + "/kyc-portal. Contact compliance-desk@netbanking-alerts.internal-demo.test for assistance.");
        ev2.setStatus("COMPLETED");
        ev2 = evidenceRepository.save(ev2);

        ExtractedIndicator ind2Url = new ExtractedIndicator(ev2, IndicatorType.URL,
                "https://" + sharedDomain + "/kyc-portal",
                "https://" + sharedDomain + "/kyc-portal",
                "DETERMINISTIC_REGEX_V1", now.minus(1, ChronoUnit.HOURS));
        ind2Url = indicatorRepository.save(ind2Url);

        ExtractedIndicator ind2Domain = new ExtractedIndicator(ev2, IndicatorType.DOMAIN,
                sharedDomain, sharedDomain,
                "DETERMINISTIC_DOMAIN_EXTRACTOR", now.minus(1, ChronoUnit.HOURS));
        ind2Domain = indicatorRepository.save(ind2Domain);

        ExtractedIndicator ind2Email = new ExtractedIndicator(ev2, IndicatorType.EMAIL_ADDRESS,
                "compliance-desk@netbanking-alerts.internal-demo.test",
                "compliance-desk@netbanking-alerts.internal-demo.test",
                "DETERMINISTIC_EMAIL_EXTRACTOR", now.minus(1, ChronoUnit.HOURS));
        indicatorRepository.save(ind2Email);

        ThreatLookup lookup2 = new ThreatLookup(
                ind2Domain,
                "VirusTotal (Synthetic Demonstration)",
                sharedDomain,
                LookupStatus.COMPLETED,
                ThreatStatus.MALICIOUS,
                "{\"positives\": 14, \"total\": 90, \"category\": \"phishing\", \"reputation\": -80}",
                "VT-DEMO-2026-8812",
                null,
                now.minus(1, ChronoUnit.HOURS)
        );
        lookupRepository.save(lookup2);

        RiskAssessment risk2 = new RiskAssessment(
                case2,
                75,
                RiskCategory.HIGH,
                0.90,
                "External email headers verified. Domain reputation verified.",
                "Targeted spear-phishing attack attempting KYC credential capture. Domain verified malicious by 14 security engines.",
                now.minus(1, ChronoUnit.HOURS)
        );
        risk2 = assessmentRepository.save(risk2);

        String ev2Ref = ev2.getId() != null ? ev2.getId().toString() : "DEMO-EV2";
        contributionRepository.save(new RiskContribution(risk2, "MALICIOUS_THREAT_INTEL", "Verified Malicious Threat Intel", 40, ev2Ref, "VirusTotal verified domain reputation as known phishing infrastructure."));
        contributionRepository.save(new RiskContribution(risk2, "IMPERSONATION", "Corporate Bank Impersonation", 20, ev2Ref, "Spoofed sender address mimicking official compliance office."));
        contributionRepository.save(new RiskContribution(risk2, "URGENCY", "24-Hour Coercion Window", 15, ev2Ref, "Time-limited urgency deadline forcing hasty victim response."));

        // ==========================================
        // CASE 3: Voice Phishing / Fraud Dept Refund
        // ==========================================
        Investigation case3 = new Investigation(
                currentUser,
                DEMO_PREFIX + "Apex Campaign — Vishing Follow-up & Fraud Refund Lure",
                "Customer Fraud Report: Fraudulent support agent follow-up message instructing user to claim unauthorized charge refund via server " + sharedIp + " and host " + sharedDomain + ".",
                InvestigationCategory.IDENTITY_FRAUD
        );
        case3.setStatus(InvestigationStatus.COMPLETED);
        case3.setRiskScore(70.0);
        case3.setOverallRiskLevel(SeverityLevel.HIGH);
        case3 = investigationRepository.save(case3);
        createdInvestigations.add(case3);

        Evidence ev3 = new Evidence(
                case3,
                EvidenceType.TEXT_MESSAGE,
                "SMS transcript received following voice call from fraudulent support."
        );
        ev3.setTextContent("Support Ticket #7821: Unauthorized transaction of $450 detected. Reply with payment transaction reference to claim refund. Server routing: " + sharedIp + ". Verification portal: https://" + sharedDomain + "/refund");
        ev3.setStatus("COMPLETED");
        ev3 = evidenceRepository.save(ev3);

        ExtractedIndicator ind3Url = new ExtractedIndicator(ev3, IndicatorType.URL,
                "https://" + sharedDomain + "/refund",
                "https://" + sharedDomain + "/refund",
                "DETERMINISTIC_REGEX_V1", now.minus(30, ChronoUnit.MINUTES));
        ind3Url = indicatorRepository.save(ind3Url);

        ExtractedIndicator ind3Domain = new ExtractedIndicator(ev3, IndicatorType.DOMAIN,
                sharedDomain, sharedDomain,
                "DETERMINISTIC_DOMAIN_EXTRACTOR", now.minus(30, ChronoUnit.MINUTES));
        ind3Domain = indicatorRepository.save(ind3Domain);

        ExtractedIndicator ind3Ip = new ExtractedIndicator(ev3, IndicatorType.IP_ADDRESS,
                sharedIp, sharedIp,
                "DETERMINISTIC_IP_EXTRACTOR", now.minus(30, ChronoUnit.MINUTES));
        ind3Ip = indicatorRepository.save(ind3Ip);

        RiskAssessment risk3 = new RiskAssessment(
                case3,
                70,
                RiskCategory.HIGH,
                0.88,
                "Verified phone transcript and routing indicators.",
                "Impersonation and advance-fee refund scam targeting victims of alleged fraud.",
                now.minus(30, ChronoUnit.MINUTES)
        );
        risk3 = assessmentRepository.save(risk3);

        String ev3Ref = ev3.getId() != null ? ev3.getId().toString() : "DEMO-EV3";
        contributionRepository.save(new RiskContribution(risk3, "IMPERSONATION", "Customer Support Impersonation", 25, ev3Ref, "Attacker masquerades as bank fraud investigation department."));
        contributionRepository.save(new RiskContribution(risk3, "PAYMENT_REFUND", "Advance Fee / Refund Scheme", 25, ev3Ref, "Solicitation of transaction reference to intercept reversal."));
        contributionRepository.save(new RiskContribution(risk3, "UNUSUAL_URL", "Unusual Banking Path", 20, ev3Ref, "Unregistered third-party domain hosting banking refund workflow."));

        // Establish Evidence Relationships
        relationshipRepository.save(new EvidenceRelationship(
                case1, null, null, ev1, ev1,
                RelationshipType.CONTAINS_URL,
                RelationshipVerificationStatus.DIRECTLY_EXTRACTED,
                1.0,
                "Deterministic extraction identified target URL payload in SMS.",
                "REGEX_EXTRACTOR", now.minus(2, ChronoUnit.HOURS)
        ));

        relationshipRepository.save(new EvidenceRelationship(
                case2, null, null, ev2, ev2,
                RelationshipType.REFERENCES_DOMAIN,
                RelationshipVerificationStatus.DIRECTLY_EXTRACTED,
                1.0,
                "Extracted URL in email links to infrastructure domain " + sharedDomain + ".",
                "DOMAIN_PARSER", now.minus(1, ChronoUnit.HOURS)
        ));

        relationshipRepository.save(new EvidenceRelationship(
                case3, null, null, ev3, ev3,
                RelationshipType.CONTAINS_URL,
                RelationshipVerificationStatus.DIRECTLY_EXTRACTED,
                1.0,
                "Extracted refund link references campaign domain " + sharedDomain + ".",
                "REGEX_EXTRACTOR", now.minus(30, ChronoUnit.MINUTES)
        ));

        return createdInvestigations;
    }

    public void resetDemoCampaign() {
        UUID userId = securityUtils.getCurrentUserId();
        List<Investigation> userCases = investigationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Investigation inv : userCases) {
            if (inv.getTitle() != null && inv.getTitle().startsWith(DEMO_PREFIX)) {
                investigationRepository.delete(inv);
            }
        }
    }

    public boolean hasDemoCampaign() {
        UUID userId = securityUtils.getCurrentUserId();
        List<Investigation> userCases = investigationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return userCases.stream().anyMatch(inv -> inv.getTitle() != null && inv.getTitle().startsWith(DEMO_PREFIX));
    }
}
