package com.phantomtrace.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalStorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should initialize and store file successfully in configured directory")
    void testStoreFile() {
        LocalStorageServiceImpl service = new LocalStorageServiceImpl(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("evidence", "sample.txt", "text/plain",
                "Forensic Data".getBytes());

        String filename = service.store(file);
        assertThat(filename).endsWith(".txt");
        assertThat(service.loadAsResource(filename).exists()).isTrue();
    }

    @Test
    @DisplayName("Should fall back gracefully when configured path cannot be created")
    void testFallbackOnPermissionDenied() {
        // Points to an invalid path on unix/mac that cannot be created as a standard
        // file path
        LocalStorageServiceImpl service = new LocalStorageServiceImpl("/root/forbidden_upload_dir_test_phantomtrace");
        MockMultipartFile file = new MockMultipartFile("evidence", "evidence.pdf", "application/pdf",
                "%PDF-1.4".getBytes());

        String filename = service.store(file);
        assertThat(filename).endsWith(".pdf");
        assertThat(service.loadAsResource(filename).exists()).isTrue();
    }
}
