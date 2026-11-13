package com.phantomtrace.service;

import com.phantomtrace.dto.evidence.CreateEvidenceRequest;
import com.phantomtrace.dto.evidence.EvidenceResponse;
import com.phantomtrace.ai.agent.SecurityManager;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.repository.EvidenceRepository;
import com.phantomtrace.repository.InvestigationRepository;
import com.phantomtrace.security.SecurityUtils;
import com.phantomtrace.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Transactional
public class EvidenceServiceImpl implements EvidenceService {

    private static final long DEFAULT_MAX_FILE_SIZE = 25L * 1024 * 1024;
    private static final int MAX_DESCRIPTION_LENGTH = 4_000;
    private static final int MAX_TEXT_LENGTH = 1_000_000;
    private static final int MAX_EMAIL_HEADERS_LENGTH = 64_000;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final byte[] PDF_SIGNATURE = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    private final EvidenceRepository evidenceRepository;
    private final InvestigationRepository investigationRepository;
    private final SecurityUtils securityUtils;
    private final FileStorageService fileStorageService;
    private final DeterministicExtractionService deterministicExtractionService;
    private final FileEvidenceProcessingService fileEvidenceProcessingService;
    private final SecurityManager securityManager;
    private final long maxFileSize;

    @Autowired
    public EvidenceServiceImpl(EvidenceRepository evidenceRepository,
            InvestigationRepository investigationRepository,
            SecurityUtils securityUtils,
            FileStorageService fileStorageService,
            DeterministicExtractionService deterministicExtractionService,
            FileEvidenceProcessingService fileEvidenceProcessingService,
            SecurityManager securityManager,
            @Value("${phantomtrace.storage.max-file-size-bytes:26214400}") long maxFileSize) {
        this.evidenceRepository = evidenceRepository;
        this.investigationRepository = investigationRepository;
        this.securityUtils = securityUtils;
        this.fileStorageService = fileStorageService;
        this.deterministicExtractionService = deterministicExtractionService;
        this.fileEvidenceProcessingService = fileEvidenceProcessingService;
        this.securityManager = securityManager;
        this.maxFileSize = maxFileSize > 0 ? maxFileSize : DEFAULT_MAX_FILE_SIZE;
    }

    public EvidenceServiceImpl(EvidenceRepository evidenceRepository,
            InvestigationRepository investigationRepository,
            SecurityUtils securityUtils,
            FileStorageService fileStorageService,
            DeterministicExtractionService deterministicExtractionService,
            FileEvidenceProcessingService fileEvidenceProcessingService,
            long maxFileSize) {
        this(evidenceRepository, investigationRepository, securityUtils, fileStorageService,
                deterministicExtractionService, fileEvidenceProcessingService, new SecurityManager(), maxFileSize);
    }

    @Override
    public EvidenceResponse create(UUID investigationId, CreateEvidenceRequest request) {
        Investigation investigation = ownedInvestigation(investigationId);
        validateStructured(request);
        Evidence evidence = new Evidence(investigation, request.evidenceType(), request.description());
        evidence.setTextContent(request.textContent());
        evidence.setUrl(request.url());
        evidence.setSenderAddress(request.senderAddress());
        evidence.setSubject(request.subject());
        evidence.setEmailBody(request.emailBody());
        evidence.setEmailHeaders(request.emailHeaders());
        evidence.setContentHash(hash(identity(request)));
        rejectDuplicate(investigationId, evidence.getContentHash());
        Evidence saved = evidenceRepository.save(evidence);
        deterministicExtractionService.extractAndPersist(saved);
        saved.setStatus("COMPLETED");
        return EvidenceResponse.fromEntity(saved);
    }

    @Override
    public EvidenceResponse createFile(UUID investigationId, CreateEvidenceRequest request, MultipartFile file) {
        Investigation investigation = ownedInvestigation(investigationId);
        validateFileRequest(request, file);
        byte[] content = readFile(file);
        validateSignature(request.evidenceType(), file.getContentType(), content);
        String contentHash = hash(content);
        rejectDuplicate(investigationId, contentHash);

        String extension = request.evidenceType() == EvidenceType.PDF ? ".pdf" : extensionForMime(file.getContentType());
        String storageKey = UUID.randomUUID() + extension;
        try {
            fileStorageService.store(new ByteArrayInputStream(content), storageKey, file.getContentType());
            Evidence evidence = new Evidence(investigation, request.evidenceType(), request.description());
            evidence.setOriginalFilename(safeFilename(file.getOriginalFilename()));
            evidence.setStorageKey(storageKey);
            evidence.setMimeType(file.getContentType());
            evidence.setFileSize((long) content.length);
            evidence.setContentHash(contentHash);
            Evidence saved = evidenceRepository.save(evidence);
            fileEvidenceProcessingService.process(saved);
            return EvidenceResponse.fromEntity(saved);
        } catch (RuntimeException ex) {
            fileStorageService.delete(storageKey);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvidenceResponse> list(UUID investigationId) {
        ownedInvestigation(investigationId);
        return evidenceRepository.findByInvestigationIdOrderByCreatedAtDesc(investigationId).stream()
                .map(EvidenceResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceResponse get(UUID evidenceId) {
        return EvidenceResponse.fromEntity(ownedEvidence(evidenceId));
    }

    @Override
    public void delete(UUID evidenceId) {
        Evidence evidence = ownedEvidence(evidenceId);
        if (evidence.getStorageKey() != null) {
            fileStorageService.delete(evidence.getStorageKey());
        }
        evidenceRepository.delete(evidence);
    }

    private Investigation ownedInvestigation(UUID investigationId) {
        UUID userId = securityUtils.getCurrentUserId();
        Investigation investigation = investigationRepository.findById(investigationId)
                .orElseThrow(() -> new ApiException("Investigation not found", HttpStatus.NOT_FOUND));
        if (!investigation.getUser().getId().equals(userId)) {
            throw new ApiException("Access denied: You do not have permission to access this investigation",
                    HttpStatus.FORBIDDEN);
        }
        return investigation;
    }

    private Evidence ownedEvidence(UUID evidenceId) {
        UUID userId = securityUtils.getCurrentUserId();
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ApiException("Evidence not found", HttpStatus.NOT_FOUND));
        if (!evidence.getInvestigation().getUser().getId().equals(userId)) {
            throw new ApiException("Access denied: You do not have permission to access this evidence",
                    HttpStatus.FORBIDDEN);
        }
        return evidence;
    }

    private void validateStructured(CreateEvidenceRequest request) {
        if (request == null || request.evidenceType() == null) {
            throw new ApiException("Evidence type is required");
        }
        maxLength(request.description(), MAX_DESCRIPTION_LENGTH, "Description");
        switch (request.evidenceType()) {
            case TEXT_MESSAGE -> {
                require(request.textContent(), "Message content is required");
                maxLength(request.textContent(), MAX_TEXT_LENGTH, "Message content");
            }
            case URL -> {
                require(request.url(), "URL is required");
                validateUrl(request.url());
            }
            case EMAIL -> {
                require(request.senderAddress(), "Sender address is required");
                require(request.subject(), "Email subject is required");
                require(request.emailBody(), "Email body is required");
                maxLength(request.subject(), MAX_DESCRIPTION_LENGTH, "Email subject");
                maxLength(request.emailBody(), MAX_TEXT_LENGTH, "Email body");
                maxLength(request.emailHeaders(), MAX_EMAIL_HEADERS_LENGTH, "Email headers");
                if (!EMAIL_PATTERN.matcher(request.senderAddress()).matches()) {
                    throw new ApiException("Sender address is invalid");
                }
            }
            default -> throw new ApiException("This evidence type requires a file upload");
        }
    }

    private void validateFileRequest(CreateEvidenceRequest request, MultipartFile file) {
        if (request == null || request.evidenceType() == null) {
            throw new ApiException("Evidence type is required");
        }
        if (request.evidenceType() != EvidenceType.IMAGE && request.evidenceType() != EvidenceType.PDF) {
            throw new ApiException("Only image and PDF evidence accepts file uploads");
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException("Evidence file is required");
        }
        if (file.getSize() > maxFileSize) {
            throw new ApiException("Evidence file exceeds the maximum allowed size");
        }
        if (file.getContentType() == null) {
            throw new ApiException("Evidence file MIME type is required");
        }
    }

    private void validateSignature(EvidenceType type, String mimeType, byte[] content) {
        if (type == EvidenceType.PDF) {
            if (!"application/pdf".equalsIgnoreCase(mimeType) || !startsWith(content, PDF_SIGNATURE)) {
                throw new ApiException("File is not a valid PDF");
            }
            return;
        }
        if (!List.of("image/png", "image/jpeg", "image/webp").contains(mimeType.toLowerCase(Locale.ROOT))
                || !validImageSignature(mimeType, content)) {
            throw new ApiException("Unsupported or invalid image file");
        }
    }

    private boolean validImageSignature(String mimeType, byte[] content) {
        return switch (mimeType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> startsWith(content, new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
            case "image/jpeg" -> startsWith(content, new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
            case "image/webp" -> content.length >= 12
                    && startsWith(content, new byte[] {'R', 'I', 'F', 'F'})
                    && new String(content, 8, 4, StandardCharsets.US_ASCII).equals("WEBP");
            default -> false;
        };
    }

    private byte[] readFile(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new ApiException("Could not read evidence file", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateUrl(String value) {
        securityManager.validateUrl(value);
    }

    private void rejectDuplicate(UUID investigationId, String contentHash) {
        if (evidenceRepository.existsByInvestigationIdAndContentHash(investigationId, contentHash)) {
            throw new ApiException("Duplicate evidence has already been submitted");
        }
    }

    private String identity(CreateEvidenceRequest request) {
        return String.join("\u0000",
                request.evidenceType().name(),
                nullToEmpty(request.textContent()),
                nullToEmpty(request.url()),
                nullToEmpty(request.senderAddress()),
                nullToEmpty(request.subject()),
                nullToEmpty(request.emailBody()),
                nullToEmpty(request.emailHeaders()));
    }

    private String hash(String value) {
        return hash(value.getBytes(StandardCharsets.UTF_8));
    }

    private String hash(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    private String extensionForMime(String mimeType) {
        return switch (mimeType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "evidence";
        }
        return Paths.get(filename).getFileName().toString();
    }

    private void require(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(message);
        }
    }

    private void maxLength(String value, int maximum, String field) {
        if (value != null && value.length() > maximum) {
            throw new ApiException(field + " exceeds the maximum allowed length");
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (value[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
