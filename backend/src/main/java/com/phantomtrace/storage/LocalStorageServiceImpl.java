package com.phantomtrace.storage;

import com.phantomtrace.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageServiceImpl implements FileStorageService {

    private final Path rootLocation;

    public LocalStorageServiceImpl(@Value("${phantomtrace.storage.local.upload-dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new ApiException("Could not initialize local storage folder", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException("Cannot store empty file", HttpStatus.BAD_REQUEST);
        }
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;
        try (InputStream inputStream = file.getInputStream()) {
            return store(inputStream, storedFilename, file.getContentType());
        } catch (IOException e) {
            throw new ApiException("Failed to read uploaded file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String store(InputStream inputStream, String filename, String contentType) {
        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename)).normalize();
            if (!destinationFile.getParent().equals(this.rootLocation)) {
                throw new ApiException("Cannot store file outside target directory", HttpStatus.BAD_REQUEST);
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new ApiException("Failed to persist file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new ApiException("File not found or unreadable: " + filename, HttpStatus.NOT_FOUND);
            }
        } catch (MalformedURLException e) {
            throw new ApiException("Error reading file path: " + filename, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public Path load(String filename) {
        Path root = rootLocation.toAbsolutePath().normalize();
        Path resolved = root.resolve(filename).normalize();
        if (!resolved.startsWith(root)) {
            throw new ApiException("Invalid storage key", HttpStatus.BAD_REQUEST);
        }
        return resolved;
    }

    @Override
    public void delete(String filename) {
        try {
            Files.deleteIfExists(load(filename));
        } catch (IOException e) {
            throw new ApiException("Failed to delete stored evidence", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
