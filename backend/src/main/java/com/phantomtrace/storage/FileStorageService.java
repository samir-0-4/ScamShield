package com.phantomtrace.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Path;

public interface FileStorageService {

    String store(MultipartFile file);

    String store(InputStream inputStream, String filename, String contentType);

    Resource loadAsResource(String filename);

    Path load(String filename);

    void delete(String filename);
}

