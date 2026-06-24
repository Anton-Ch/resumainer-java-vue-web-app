package com.resumainer.model;

/**
 * Result of a public resume lookup.
 * <p>
 * Provides both a status and the physical PDF path (when ACTIVE).
 * Replaces the earlier null-only return that could not distinguish
 * between not-found, deleted, missing-file, and unsafe-path states.
 */
public class PublicResumeLookupResult {

    private final Status status;
    private final String pdfFilePath;

    public PublicResumeLookupResult(Status status, String pdfFilePath) {
        this.status = status;
        this.pdfFilePath = pdfFilePath;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Returns the safe PDF file path, or null when the status is not ACTIVE.
     */
    public String getPdfFilePath() {
        return pdfFilePath;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    /**
     * All non-ACTIVE status codes — the caller should use the corresponding HTTP
     * response helper rather than serving the PDF.
     */
    public enum Status {
        ACTIVE,
        DELETED,
        NOT_FOUND,
        MISSING_FILE,
        UNSAFE_PATH
    }
}
