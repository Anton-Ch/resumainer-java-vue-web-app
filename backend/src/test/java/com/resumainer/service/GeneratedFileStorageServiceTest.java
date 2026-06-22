package com.resumainer.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GeneratedFileStorageService.
 * Tests file operations with real filesystem, cleaning up after each test.
 */
class GeneratedFileStorageServiceTest {

    private final GeneratedFileStorageService service = new GeneratedFileStorageService();

    private final String testUsername = "testuser_" + System.currentTimeMillis();
    private final String testPublicCode = "testcode_" + System.currentTimeMillis();
    private final String testFilename = "resume_en_balanced.html";
    private final String testContent = "<html><body>Test Resume</body></html>";

    @BeforeEach
    void setUp() throws IOException {
        // Ensure clean state before each test
        cleanupGeneratedResults();
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanupGeneratedResults();
    }

    private void cleanupGeneratedResults() throws IOException {
        Path base = Paths.get("generated_results");
        if (Files.exists(base)) {
            Files.walk(base)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }

    @Test
    void saveFile_createsFileWithContent() {
        String relativePath = service.saveFile(testUsername, testPublicCode, testFilename, testContent);

        assertNotNull(relativePath);
        assertTrue(relativePath.contains("generated_results"));
        assertTrue(relativePath.contains(testUsername));
        assertTrue(relativePath.contains(testPublicCode));
        assertTrue(relativePath.contains(testFilename));
        assertTrue(relativePath.replace("\\", "/").endsWith(testFilename));

        // Verify file exists and content matches
        String actual = service.readFile(relativePath);
        assertEquals(testContent, actual);
    }

    @Test
    void saveFile_withNullUsername_usesUnknown() {
        String relativePath = service.saveFile(null, testPublicCode, testFilename, testContent);

        assertTrue(relativePath.contains("unknown"));
        String actual = service.readFile(relativePath);
        assertEquals(testContent, actual);
    }

    @Test
    void saveFile_withBlankUsername_usesUnknown() {
        String relativePath = service.saveFile("", testPublicCode, testFilename, testContent);

        assertTrue(relativePath.contains("unknown"));
        String actual = service.readFile(relativePath);
        assertEquals(testContent, actual);
    }

    @Test
    void saveFile_withPathTraversalInUsername_sanitizes() {
        String maliciousUser = "../../etc/passwd";
        String relativePath = service.saveFile(maliciousUser, testPublicCode, testFilename, testContent);

        // Should NOT contain ".." after sanitization
        assertFalse(relativePath.contains(".."), "Path should not contain traversal sequences");
        assertTrue(relativePath.contains("etc_passwd") || relativePath.contains("____etc_passwd"),
                "Traversal chars should be replaced");

        // File should be inside generated_results
        String actual = service.readFile(relativePath);
        assertEquals(testContent, actual);
    }

    @Test
    void saveFile_withEmptyContent_writesEmptyFile() {
        String relativePath = service.saveFile(testUsername, testPublicCode, testFilename, "");

        String actual = service.readFile(relativePath);
        assertEquals("", actual);
    }

    @Test
    void saveFile_withBlankFilename_usesDefault() {
        String relativePath = service.saveFile(testUsername, testPublicCode, "", testContent);

        assertTrue(relativePath.replace("\\", "/").endsWith("file.html"));
    }

    @Test
    void saveFile_withSpecialCharsInFilename_sanitizesHtmlTags() {
        String unsafeFilename = "malicious<script>.html";
        String relativePath = service.saveFile(testUsername, testPublicCode, unsafeFilename, testContent);

        assertFalse(relativePath.contains("<"), "HTML tag chars should be sanitized");
        assertFalse(relativePath.contains(">"), "HTML tag chars should be sanitized");
        // Note: dots are allowed in filenames by the service
        assertTrue(relativePath.replace("\\", "/").endsWith(".html"));
    }

    @Test
    void readFile_withExistingFile_returnsContent() {
        // First save a file, then read it
        String relativePath = service.saveFile(testUsername, testPublicCode, testFilename, testContent);

        String result = service.readFile(relativePath);
        assertEquals(testContent, result);
    }

    @Test
    void readFile_withNonExistentPath_throwsRuntimeException() {
        assertThrows(RuntimeException.class,
                () -> service.readFile("generated_results/nonexistent/file.html"));
    }

    @Test
    void resolvePath_returnsNormalizedPath() {
        Path result = service.resolvePath("generated_results/../test/file.html");

        assertEquals(Paths.get("test/file.html"), result);
    }

    @Test
    void sanitizePathSegment_withNull_returnsUnknown() {
        // Call saveFile with null to trigger sanitizePathSegment
        String relativePath = service.saveFile(null, "code", "f.html", "content");
        assertTrue(relativePath.contains("unknown"));
    }

    @Test
    void sanitizePathSegment_withBackslashTraversal_sanitizes() {
        // Windows path traversal with backslashes
        String malicious = "..\\..\\etc";
        String relativePath = service.saveFile(malicious, "code", "f.html", "content");
        assertFalse(relativePath.contains(".."), "Backslash traversal should be sanitized");
    }

    // ── resolveSafePath tests (Blocker B fix) ──────────────────────────

    @Test
    void resolveSafePath_simpleRelative_resolvesCorrectly() {
        Path result = service.resolveSafePath("user1/CODE1/file.pdf");
        assertNotNull(result);
        assertTrue(result.endsWith(Paths.get("user1/CODE1/file.pdf")));
    }

    @Test
    void resolveSafePath_generatedResultsPrefix_stripsAndResolves() {
        Path result = service.resolveSafePath("generated_results/user1/CODE1/file.pdf");
        assertNotNull(result);
        assertTrue(result.endsWith(Paths.get("user1/CODE1/file.pdf")));
    }

    @Test
    void resolveSafePath_absolutePathInsideStorageRoot_resolvesCorrectly() throws IOException {
        // Create a file under the storage root
        Path storageRoot = Paths.get("generated_results").toAbsolutePath().normalize();
        Path testDir = storageRoot.resolve("absTest");
        Files.createDirectories(testDir);
        Path testFile = testDir.resolve("test.pdf");
        Files.writeString(testFile, "test");
        try {
            String absolutePath = testFile.toString();
            Path result = service.resolveSafePath(absolutePath);
            assertNotNull(result);
            assertTrue(Files.exists(result), "Resolved path should exist when inside storage root");
        } finally {
            Files.walk(testDir).sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
            });
        }
    }

    @Test
    void resolveSafePath_absolutePathOutsideStorageRoot_rejects() {
        // An absolute path outside generated_results should be rejected
        String outsidePath = System.getProperty("os.name").toLowerCase().contains("win")
                ? "C:\\Windows\\System32\\config\\SAM"
                : "/etc/passwd";
        assertThrows(SecurityException.class, () -> service.resolveSafePath(outsidePath));
    }

    @Test
    void resolveSafePath_traversal_rejects() {
        assertThrows(SecurityException.class,
                () -> service.resolveSafePath("generated_results/../../etc/passwd"));
    }

    @Test
    void resolveSafePath_nullPath_rejects() {
        assertThrows(SecurityException.class,
                () -> service.resolveSafePath(null));
    }

    @Test
    void resolveSafePath_blankPath_rejects() {
        assertThrows(SecurityException.class,
                () -> service.resolveSafePath("  "));
    }
}
