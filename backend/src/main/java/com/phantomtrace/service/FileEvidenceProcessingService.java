package com.phantomtrace.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.phantomtrace.entity.Evidence;
import com.phantomtrace.exception.ApiException;
import com.phantomtrace.storage.FileStorageService;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileEvidenceProcessingService {
    private static final int MAX_OCR_TEXT = 100_000;

    private final FileStorageService storageService;
    private final DeterministicExtractionService extractionService;

    public FileEvidenceProcessingService(FileStorageService storageService,
            DeterministicExtractionService extractionService) {
        this.storageService = storageService;
        this.extractionService = extractionService;
    }

    @Transactional
    public void process(Evidence evidence) {
        evidence.setStatus("PROCESSING");
        try {
            byte[] content = Files.readAllBytes(storageService.load(evidence.getStorageKey()));
            String extracted = evidence.getEvidenceType().name().equals("PDF")
                    ? extractPdf(content)
                    : extractImage(content);
            evidence.setNormalizedContent(limit(extracted));
            extractionService.extractAndPersist(evidence);
            evidence.setStatus("COMPLETED");
        } catch (Exception ex) {
            evidence.setStatus("FAILED");
            throw new ApiException(safeMessage(ex), HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private String extractPdf(byte[] content) throws IOException {
        try (PDDocument document = Loader.loadPDF(content)) {
            if (document.isEncrypted()) {
                throw new IOException("Password-protected PDF cannot be analyzed");
            }
            return new PDFTextStripper().getText(document);
        } catch (InvalidPasswordException ex) {
            throw new IOException("Password-protected PDF cannot be analyzed", ex);
        }
    }

    private String extractImage(byte[] content) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
        if (image == null) {
            throw new IOException("Image format could not be decoded");
        }
        List<String> results = new ArrayList<>();
        try {
            Result qr = new MultiFormatReader().decode(new BinaryBitmap(
                    new HybridBinarizer(new BufferedImageLuminanceSource(image))));
            results.add("QR_PAYLOAD: " + qr.getText());
        } catch (NotFoundException ignored) {
            // A valid image does not have to contain a QR code.
        }
        try {
            Tesseract tesseract = new Tesseract();
            String text = tesseract.doOCR(image);
            if (text != null && !text.isBlank()) {
                results.add("OCR_TEXT:\n" + text);
            }
        } catch (TesseractException | LinkageError ex) {
            results.add("OCR_UNAVAILABLE: Tesseract runtime is not installed");
        }
        return String.join("\n", results);
    }

    private String limit(String value) {
        if (value == null) return "";
        return value.length() > MAX_OCR_TEXT ? value.substring(0, MAX_OCR_TEXT) : value;
    }

    private String safeMessage(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Evidence file processing failed" : ex.getMessage();
    }
}
