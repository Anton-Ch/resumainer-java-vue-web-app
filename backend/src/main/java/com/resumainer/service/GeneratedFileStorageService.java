package com.resumainer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Server-side file storage for generated HTML artifacts.
 * Builds safe paths under generated_results/{username}/{public_code}/.
 * Strips path traversal sequences from username segments.
 */
@Service
public class GeneratedFileStorageService {

    private static final Logger log = LoggerFactory.getLogger(GeneratedFileStorageService.class);

    private static final String BASE_DIR = "generated_results";

    /**
     * Saves content to a file under the user's directory.
     *
     * @param username   the owner's username (sanitized for path safety)
     * @param publicCode the unique public code for this resume
     * @param filename   the filename (e.g., "2026-06-12_en_balanced.html")
     * @param content    the file content (UTF-8)
     * @return the relative path to the saved file
     */
    public String saveFile(String username, String publicCode, String filename, String content) {
        String safeUsername = sanitizePathSegment(username);
        String safeCode = sanitizePathSegment(publicCode);
        String safeFilename = sanitizeFilename(filename);

        Path basePath = Paths.get(BASE_DIR, safeUsername, safeCode).normalize();

        // Security: verify the resolved path is inside BASE_DIR
        if (!basePath.startsWith(Paths.get(BASE_DIR).normalize())) {
            throw new SecurityException("Path traversal detected for username: " + username);
        }

        try {
            Files.createDirectories(basePath);
            Path filePath = basePath.resolve(safeFilename);
            Files.writeString(filePath, content, StandardCharsets.UTF_8);
            log.debug("File saved: {}", filePath);
            return filePath.toString().replace("\\", "/");
        } catch (IOException e) {
            log.error("Failed to save file: {}", filename, e);
            throw new RuntimeException("Failed to save generated file. Please try again.");
        }
    }

    /**
     * Reads a file from a relative path and returns its content.
     */
    public String readFile(String relativePath) {
        try {
            Path filePath = Paths.get(relativePath).normalize();
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read file: {}", relativePath, e);
            throw new RuntimeException("File not found.");
        }
    }

    /**
     * Returns the full path for a relative file path.
     */
    public Path resolvePath(String relativePath) {
        return Paths.get(relativePath).normalize();
    }

    /**
     * Resolves and validates a stored file path against the storage root.
     * Rejects null, absolute, and traversal paths. Returns safe normalized Path.
     * @throws SecurityException if path is unsafe
     */
    public Path resolveSafePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            throw new SecurityException("Path is null or blank");
        }
        // Strip BASE_DIR prefix if already present (paths stored relative to BASE_DIR)
        String relative = storedPath;
        if (relative.startsWith(BASE_DIR + "/") || relative.startsWith(BASE_DIR + "\\")) {
            relative = relative.substring(BASE_DIR.length() + 1);
        }
        Path resolved = Paths.get(BASE_DIR, relative).normalize();
        Path root = Paths.get(BASE_DIR).normalize().toAbsolutePath();
        if (!resolved.toAbsolutePath().normalize().startsWith(root)) {
            throw new SecurityException("Path traversal detected: " + storedPath);
        }
        return resolved;
    }

    private String sanitizePathSegment(String segment) {
        if (segment == null || segment.isBlank()) return "unknown";
        // Remove path traversal sequences and non-alphanumeric chars
        String safe = segment
                .replaceAll("\\.\\./", "")
                .replaceAll("\\.\\.\\\\", "")
                .replaceAll("/", "_")
                .replaceAll("\\\\", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe;
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "file.html";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
