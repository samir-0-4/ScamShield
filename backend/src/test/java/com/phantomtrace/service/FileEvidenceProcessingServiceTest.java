package com.phantomtrace.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.entity.Investigation;
import com.phantomtrace.entity.User;
import com.phantomtrace.enums.EvidenceType;
import com.phantomtrace.enums.InvestigationCategory;
import com.phantomtrace.enums.UserRole;
import com.phantomtrace.storage.FileStorageService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileEvidenceProcessingServiceTest {
    @Mock FileStorageService storageService;
    @Mock DeterministicExtractionService extractionService;

    @Test
    void extractsPdfTextAndMarksEvidenceCompleted() throws Exception {
        byte[] pdf = pdf("Suspicious offer https://example.test");
        Path path = Files.createTempFile("phantomtrace-", ".pdf");
        Files.write(path, pdf);
        when(storageService.load("case.pdf")).thenReturn(path);

        Evidence evidence = evidence(EvidenceType.PDF);
        evidence.setStorageKey("case.pdf");
        new FileEvidenceProcessingService(storageService, extractionService).process(evidence);

        assertThat(evidence.getStatus()).isEqualTo("COMPLETED");
        assertThat(evidence.getNormalizedContent()).contains("Suspicious offer");
        verify(extractionService).extractAndPersist(evidence);
        Files.deleteIfExists(path);
    }

    @Test
    void rejectsCorruptedPdfAndMarksEvidenceFailed() throws Exception {
        Path path = Files.createTempFile("phantomtrace-", ".pdf");
        Files.writeString(path, "not a pdf");
        when(storageService.load("case.pdf")).thenReturn(path);
        Evidence evidence = evidence(EvidenceType.PDF);
        evidence.setStorageKey("case.pdf");

        assertThatThrownBy(() -> new FileEvidenceProcessingService(storageService, extractionService).process(evidence))
                .isInstanceOf(com.phantomtrace.exception.ApiException.class);
        assertThat(evidence.getStatus()).isEqualTo("FAILED");
        verifyNoInteractions(extractionService);
        Files.deleteIfExists(path);
    }

    @Test
    void decodesQrPayloadWithoutVisitingTheDecodedUrl() throws Exception {
        var matrix = new QRCodeWriter().encode("https://demo.invalid/qr",
                BarcodeFormat.QR_CODE, 120, 120);
        Path path = Files.createTempFile("phantomtrace-", ".png");
        try (var output = Files.newOutputStream(path)) {
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
        }
        when(storageService.load("case.png")).thenReturn(path);
        Evidence evidence = evidence(EvidenceType.IMAGE);
        evidence.setStorageKey("case.png");

        new FileEvidenceProcessingService(storageService, extractionService).process(evidence);

        assertThat(evidence.getStatus()).isEqualTo("COMPLETED");
        assertThat(evidence.getNormalizedContent()).contains("QR_PAYLOAD: https://demo.invalid/qr");
        verify(extractionService).extractAndPersist(evidence);
        Files.deleteIfExists(path);
    }

    private Evidence evidence(EvidenceType type) {
        User user = new User("owner@example.com", "hash", "Owner", UserRole.ROLE_ANALYST);
        Investigation investigation = new Investigation(user, "Case", "Context", InvestigationCategory.PHISHING);
        Evidence evidence = new Evidence(investigation, type, "file");
        ReflectionTestUtils.setField(evidence, "id", UUID.randomUUID());
        return evidence;
    }

    private byte[] pdf(String text) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(text);
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
