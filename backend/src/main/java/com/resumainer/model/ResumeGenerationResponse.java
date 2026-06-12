package com.resumainer.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AI-generated response for one language + one adaptation level.
 * Maps to 'resume_generation_response' table (UUID PK).
 * Status: DRAFT (AI returned) or FINALIZED (user approved).
 */
public class ResumeGenerationResponse {

    private UUID id;
    private UUID generationRequestId;

    private long languageId;
    private long adaptationLevelId;
    private long statusId;

    private String professionalTitle;
    private String valueLine;
    private String professionalSummary;
    private String professionalAspirations;
    private String coverLetter;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumeGenerationResponse() {
    }

    // --- Getters and Setters ---

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getGenerationRequestId() { return generationRequestId; }
    public void setGenerationRequestId(UUID generationRequestId) { this.generationRequestId = generationRequestId; }

    public long getLanguageId() { return languageId; }
    public void setLanguageId(long languageId) { this.languageId = languageId; }

    public long getAdaptationLevelId() { return adaptationLevelId; }
    public void setAdaptationLevelId(long adaptationLevelId) { this.adaptationLevelId = adaptationLevelId; }

    public long getStatusId() { return statusId; }
    public void setStatusId(long statusId) { this.statusId = statusId; }

    public String getProfessionalTitle() { return professionalTitle; }
    public void setProfessionalTitle(String professionalTitle) { this.professionalTitle = professionalTitle; }

    public String getValueLine() { return valueLine; }
    public void setValueLine(String valueLine) { this.valueLine = valueLine; }

    public String getProfessionalSummary() { return professionalSummary; }
    public void setProfessionalSummary(String professionalSummary) { this.professionalSummary = professionalSummary; }

    public String getProfessionalAspirations() { return professionalAspirations; }
    public void setProfessionalAspirations(String professionalAspirations) { this.professionalAspirations = professionalAspirations; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
